package com.infotact.fleet.service;

import com.infotact.fleet.api.dto.DeliveryTaskResponse;
import com.infotact.fleet.api.dto.FleetDashboardResponse;
import com.infotact.fleet.api.dto.RouteOptimizationRequest;
import com.infotact.fleet.api.dto.RouteResponse;
import com.infotact.fleet.domain.*;
import com.infotact.fleet.exception.ResourceNotFoundException;
import com.infotact.fleet.repository.*;
import com.infotact.fleet.service.optimization.TwoOptStrategy;
import com.infotact.fleet.service.optimization.RouteScoringSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@SuppressWarnings("null")
public class RouteOptimizationService {

    private final RouteRepository routeRepository;
    private final DeliveryTaskRepository taskRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final DeliveryTaskService deliveryTaskService;
    private final OsrmClient osrmClient;
    private final AuditService auditService;
    private final TwoOptStrategy routeOptimizationStrategy;
    private final RouteScoringSystem routeScoringSystem;

    @Value("${fleet.fuel-efficiency.diesel-km-per-liter:8.0}")
    private double dieselKmPerLiter;

    @Value("${fleet.fuel-efficiency.petrol-km-per-liter:10.0}")
    private double petrolKmPerLiter;

    @Value("${fleet.route.average-speed-kmh:40.0}")
    private double averageRouteSpeedKmh;

    @Value("${fleet.osrm.max-waypoints:12}")
    private int maxWaypoints;

    public RouteOptimizationService(RouteRepository routeRepository,
                                    DeliveryTaskRepository taskRepository,
                                    VehicleRepository vehicleRepository,
                                    DriverRepository driverRepository,
                                    DeliveryTaskService deliveryTaskService,
                                    OsrmClient osrmClient,
                                    AuditService auditService,
                                    TwoOptStrategy routeOptimizationStrategy,
                                    RouteScoringSystem routeScoringSystem) {
        this.routeRepository = routeRepository;
        this.taskRepository = taskRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.deliveryTaskService = deliveryTaskService;
        this.osrmClient = osrmClient;
        this.auditService = auditService;
        this.routeOptimizationStrategy = routeOptimizationStrategy;
        this.routeScoringSystem = routeScoringSystem;
    }

    public List<RouteResponse> listAll() {
        return routeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RouteResponse> listAll(String status, String search, org.springframework.data.domain.Pageable pageable) {
        RouteStatus routeStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                routeStatus = RouteStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        }
        String searchQuery = (search != null && !search.isBlank()) ? "%" + search.trim().toLowerCase() + "%" : null;
        return routeRepository.searchRoutes(routeStatus, searchQuery, pageable)
                .map(this::toResponse)
                .getContent();
    }

    public RouteResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public RouteResponse optimizeRoute(RouteOptimizationRequest request) {
        Set<Long> uniqueTaskIds = new HashSet<>(request.deliveryTaskIds());
        if (uniqueTaskIds.size() != request.deliveryTaskIds().size()) {
            throw new IllegalArgumentException("Delivery task list contains duplicate stops.");
        }

        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + request.vehicleId()));
        Driver driver = driverRepository.findById(request.driverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found: " + request.driverId()));

        List<DeliveryTask> tasks = request.deliveryTaskIds().stream()
                .map(id -> taskRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Delivery task not found: " + id)))
                .toList();

        if (tasks.isEmpty()) {
            throw new IllegalArgumentException("At least one delivery task is required.");
        }
        if (tasks.size() + 1 > maxWaypoints) {
            throw new IllegalArgumentException("Route exceeds maximum supported waypoint count of " + maxWaypoints + ".");
        }

        Instant plannedDeparture = request.plannedDepartureTime() != null ? request.plannedDepartureTime() : Instant.now();
        validateRouteInputs(vehicle, driver, tasks, plannedDeparture);

        // Determine depot coordinates (Default Bengaluru coordinates if not provided)
        double depotLat = request.startLatitude() != null ? request.startLatitude() : 12.9716;
        double depotLng = request.startLongitude() != null ? request.startLongitude() : 77.5946;

        // Build coordinates: [depot, task1, task2, ...]
        List<double[]> coordinates = new ArrayList<>();
        coordinates.add(new double[]{depotLng, depotLat});
        for (DeliveryTask task : tasks) {
            coordinates.add(new double[]{task.getLongitude(), task.getLatitude()});
        }

        // Fetch distance matrix from OSRM
        Map<String, double[][]> matrix = osrmClient.getDistanceMatrix(coordinates);
        double[][] distanceMatrix = matrix.get("distances");

        // Apply pluggable route optimization strategy
        int[] order = routeOptimizationStrategy.optimize(distanceMatrix, tasks, vehicle, depotLat, depotLng);
        validateTimeWindows(tasks, order, distanceMatrix, plannedDeparture);

        // Build optimized coordinate list for route summary
        List<double[]> optimizedCoords = new ArrayList<>();
        optimizedCoords.add(coordinates.get(0)); // depot
        for (int i = 1; i < order.length; i++) {
            optimizedCoords.add(coordinates.get(order[i]));
        }

        // Get route summary (total distance + duration)
        Map<String, Double> routeSummary = osrmClient.getRouteSummary(optimizedCoords);
        double totalDistanceKm = routeSummary.get("distance") / 1000.0;
        int estimatedMinutes = (int) Math.ceil(routeSummary.get("duration") / 60.0);
        validateRouteCompletesWithinShift(driver, plannedDeparture, estimatedMinutes);

        // Calculate fuel estimate
        double fuelEstimate = calculateFuelEstimate(totalDistanceKm, vehicle.getFuelType());

        // Generate route name
        String routeName = generateRouteName();

        // Create route entity
        Route route = new Route(routeName, vehicle, driver, depotLat, depotLng);
        route.setOptimizationResult(
                Math.round(totalDistanceKm * 100.0) / 100.0,
                estimatedMinutes,
                Math.round(fuelEstimate * 100.0) / 100.0,
                buildWaypointOrderJson(tasks, order)
        );

        // Calculate dynamic route score
        double trafficPenalty = Math.random() * 0.15; // simulated minor traffic penalty
        double score = routeScoringSystem.calculateScore(route, tasks, trafficPenalty);
        route.setRouteScore(score);

        route = routeRepository.save(route);

        // Assign tasks to route in optimized order
        for (int i = 1; i < order.length; i++) {
            int taskIndex = order[i] - 1; // -1 because depot is at index 0
            DeliveryTask task = tasks.get(taskIndex);
            task.assignToRoute(route, i);
            taskRepository.save(task);
        }

        auditService.log("ROUTE_OPTIMIZE", "Optimized route: " + route.getRouteName() + " (Stops: " + tasks.size() + ", Dist: " + route.getTotalDistanceKm() + " km, Score: " + route.getRouteScore() + ")");
        return toResponse(route);
    }

    @Transactional
    public RouteResponse dispatchRoute(Long routeId) {
        Route route = findOrThrow(routeId);
        route.dispatch();

        // Update driver status to ON_ROUTE
        Driver driver = route.getDriver();
        driver.updateStatus(DriverStatus.ON_ROUTE);
        driverRepository.save(driver);

        // Transition all tasks to DISPATCHED
        List<DeliveryTask> tasks = taskRepository.findAllByRouteId(routeId);
        for (DeliveryTask task : tasks) {
            if (task.getDeliveryStatus() == DeliveryStatus.UNASSIGNED) {
                task.transitionStatus(DeliveryStatus.DISPATCHED);
                taskRepository.save(task);
            }
        }

        auditService.log("ROUTE_DISPATCH", "Dispatched route: " + route.getRouteName() + " with driver " + driver.getName() + " and vehicle " + route.getVehicle().getLicensePlate());
        return toResponse(routeRepository.save(route));
    }

    @Transactional
    public RouteResponse completeRoute(Long routeId) {
        Route route = findOrThrow(routeId);
        route.complete();

        // Update driver status back to AVAILABLE
        Driver driver = route.getDriver();
        driver.updateStatus(DriverStatus.AVAILABLE);
        driverRepository.save(driver);

        // Update vehicle odometer
        if (route.getTotalDistanceKm() != null) {
            route.getVehicle().addOdometerKm(route.getTotalDistanceKm());
            vehicleRepository.save(route.getVehicle());
        }

        auditService.log("ROUTE_COMPLETE", "Completed route: " + route.getRouteName() + " (Odometer updated by " + route.getTotalDistanceKm() + " km)");
        return toResponse(routeRepository.save(route));
    }

    public FleetDashboardResponse getDashboard() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        List<Driver> drivers = driverRepository.findAll();
        List<DeliveryTask> tasks = taskRepository.findAll();
        List<Route> routes = routeRepository.findAll();

        return new FleetDashboardResponse(
                vehicles.size(),
                vehicles.stream().filter(v -> v.getMaintenanceStatus() == VehicleMaintenanceStatus.OPERATIONAL).count(),
                vehicles.stream().filter(v -> v.getMaintenanceStatus() == VehicleMaintenanceStatus.IN_MAINTENANCE).count(),
                drivers.size(),
                drivers.stream().filter(d -> d.getStatus() == DriverStatus.AVAILABLE).count(),
                drivers.stream().filter(d -> d.getStatus() == DriverStatus.ON_ROUTE).count(),
                tasks.size(),
                tasks.stream().filter(t -> t.getDeliveryStatus() == DeliveryStatus.UNASSIGNED).count(),
                tasks.stream().filter(t -> t.getDeliveryStatus() == DeliveryStatus.IN_TRANSIT).count(),
                tasks.stream().filter(t -> t.getDeliveryStatus() == DeliveryStatus.DELIVERED).count(),
                routes.stream().filter(r -> r.getStatus() == RouteStatus.ACTIVE).count(),
                routes.stream().filter(r -> r.getStatus() == RouteStatus.PLANNED).count(),
                routes.stream().filter(r -> r.getStatus() == RouteStatus.COMPLETED).count()
        );
    }

    private void validateRouteInputs(Vehicle vehicle, Driver driver, List<DeliveryTask> tasks, Instant plannedDeparture) {
        if (vehicle.getMaintenanceStatus() != VehicleMaintenanceStatus.OPERATIONAL) {
            throw new IllegalArgumentException("Vehicle " + vehicle.getLicensePlate() + " is not operational.");
        }
        if (routeRepository.existsByVehicleIdAndStatus(vehicle.getId(), RouteStatus.ACTIVE)) {
            throw new IllegalArgumentException("Vehicle " + vehicle.getLicensePlate() + " is already assigned to an active route.");
        }
        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            throw new IllegalArgumentException("Driver " + driver.getName() + " is not available.");
        }
        if (!driver.isLicenseValid()) {
            throw new IllegalArgumentException("Driver " + driver.getName() + " has an expired license.");
        }
        if (routeRepository.existsByDriverIdAndStatus(driver.getId(), RouteStatus.ACTIVE)) {
            throw new IllegalArgumentException("Driver " + driver.getName() + " is already assigned to an active route.");
        }
        if (driver.getAssignedVehicle() != null && driver.getAssignedVehicle().getId() != null
                && vehicle.getId() != null && !driver.getAssignedVehicle().getId().equals(vehicle.getId())) {
            throw new IllegalArgumentException("Driver " + driver.getName() + " is assigned to vehicle "
                    + driver.getAssignedVehicle().getLicensePlate() + ", not " + vehicle.getLicensePlate() + ".");
        }
        if (!isInsideShift(driver, plannedDeparture)) {
            throw new IllegalArgumentException("Planned departure is outside driver " + driver.getName() + "'s configured shift.");
        }

        double totalWeightKg = 0.0;
        double totalVolumeCbm = 0.0;
        for (DeliveryTask task : tasks) {
            if (task.getRoute() != null || task.getDeliveryStatus() != DeliveryStatus.UNASSIGNED) {
                throw new IllegalArgumentException("Delivery task " + task.getId() + " is not available for route planning.");
            }
            if (task.getPackageWeightKg() != null) {
                totalWeightKg += task.getPackageWeightKg();
            }
            if (task.getPackageVolumeCbm() != null) {
                totalVolumeCbm += task.getPackageVolumeCbm();
            }
        }

        if (totalWeightKg > vehicle.getCapacityKg()) {
            throw new IllegalArgumentException("Selected deliveries weigh " + round(totalWeightKg)
                    + " kg, exceeding vehicle capacity of " + round(vehicle.getCapacityKg()) + " kg.");
        }
        if (totalVolumeCbm > 0 && vehicle.getCapacityVolumeCbm() == null) {
            throw new IllegalArgumentException("Selected deliveries require volume capacity, but the vehicle volume capacity is not configured.");
        }
        if (vehicle.getCapacityVolumeCbm() != null && totalVolumeCbm > vehicle.getCapacityVolumeCbm()) {
            throw new IllegalArgumentException("Selected deliveries use " + round(totalVolumeCbm)
                    + " m3, exceeding vehicle volume capacity of " + round(vehicle.getCapacityVolumeCbm()) + " m3.");
        }
    }

    private void validateTimeWindows(List<DeliveryTask> tasks, int[] order, double[][] distanceMatrix, Instant plannedDeparture) {
        Instant currentArrival = plannedDeparture;
        double speedMetersPerSecond = (averageRouteSpeedKmh * 1000.0) / 3600.0;

        for (int i = 1; i < order.length; i++) {
            int previousIndex = order[i - 1];
            int currentIndex = order[i];
            long travelSeconds = (long) Math.ceil(distanceMatrix[previousIndex][currentIndex] / speedMetersPerSecond);
            currentArrival = currentArrival.plusSeconds(Math.max(0, travelSeconds));

            DeliveryTask task = tasks.get(currentIndex - 1);
            if (task.getTimeWindowStart() != null && currentArrival.isBefore(task.getTimeWindowStart())) {
                currentArrival = task.getTimeWindowStart();
            }
            if (task.getTimeWindowEnd() != null && currentArrival.isAfter(task.getTimeWindowEnd())) {
                throw new IllegalArgumentException("Delivery task " + task.getId()
                        + " cannot be reached before its delivery time window closes.");
            }
        }
    }

    private void validateRouteCompletesWithinShift(Driver driver, Instant plannedDeparture, int estimatedMinutes) {
        if (driver.getShiftStart() == null || driver.getShiftEnd() == null) {
            return;
        }
        Instant estimatedCompletion = plannedDeparture.plusSeconds(estimatedMinutes * 60L);
        if (!isInsideShift(driver, estimatedCompletion)) {
            throw new IllegalArgumentException("Estimated route completion is outside driver " + driver.getName() + "'s configured shift.");
        }
    }

    private boolean isInsideShift(Driver driver, Instant instant) {
        if (driver.getShiftStart() == null || driver.getShiftEnd() == null) {
            return true;
        }
        LocalTime time = instant.atZone(ZoneId.systemDefault()).toLocalTime();
        return isWithinShift(time, driver.getShiftStart(), driver.getShiftEnd());
    }

    private boolean isWithinShift(LocalTime time, LocalTime start, LocalTime end) {
        if (start.equals(end)) {
            return false;
        }
        if (start.isBefore(end)) {
            return !time.isBefore(start) && !time.isAfter(end);
        }
        return !time.isBefore(start) || !time.isAfter(end);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double calculateFuelEstimate(double distanceKm, String fuelType) {
        return switch (fuelType.toUpperCase()) {
            case "DIESEL" -> distanceKm / dieselKmPerLiter;
            case "PETROL" -> distanceKm / petrolKmPerLiter;
            case "ELECTRIC" -> distanceKm * 0.2; // kWh
            case "CNG" -> distanceKm * 0.25; // kg
            default -> distanceKm / 8.0;
        };
    }

    private String generateRouteName() {
        String datePrefix = "RT-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = routeRepository.countByRouteNameStartingWith(datePrefix) + 1;
        return datePrefix + "-" + String.format("%03d", count);
    }

    private String buildWaypointOrderJson(List<DeliveryTask> tasks, int[] order) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 1; i < order.length; i++) {
            if (i > 1) {
                sb.append(",");
            }
            sb.append(tasks.get(order[i] - 1).getId());
        }
        sb.append("]");
        return sb.toString();
    }

    private Route findOrThrow(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found: " + id));
    }

    private RouteResponse toResponse(Route r) {
        List<DeliveryTask> tasks = taskRepository.findAllByRouteId(r.getId());
        List<DeliveryTaskResponse> stops = tasks.stream()
                .sorted(Comparator.comparingInt(t -> t.getSequenceIndex() != null ? t.getSequenceIndex() : 0))
                .map(deliveryTaskService::toResponse)
                .toList();

        return new RouteResponse(
                r.getId(),
                r.getRouteName(),
                r.getVehicle() != null ? r.getVehicle().getId() : null,
                r.getVehicle() != null ? r.getVehicle().getLicensePlate() : null,
                r.getDriver() != null ? r.getDriver().getId() : null,
                r.getDriver() != null ? r.getDriver().getName() : null,
                r.getStatus().name(),
                r.getTotalDistanceKm(),
                r.getEstimatedDurationMinutes(),
                r.getTotalFuelEstimateLiters(),
                r.getRouteScore(),
                r.getStartLatitude(),
                r.getStartLongitude(),
                stops.size(),
                stops,
                r.getDispatchedAt(),
                r.getCompletedAt(),
                r.getCreatedAt()
        );
    }
}
