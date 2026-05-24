package com.infotact.fleet.service;

import com.infotact.fleet.api.dto.DeliveryTaskResponse;
import com.infotact.fleet.api.dto.FleetDashboardResponse;
import com.infotact.fleet.api.dto.RouteOptimizationRequest;
import com.infotact.fleet.api.dto.RouteResponse;
import com.infotact.fleet.domain.*;
import com.infotact.fleet.exception.ResourceNotFoundException;
import com.infotact.fleet.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class RouteOptimizationService {

    private final RouteRepository routeRepository;
    private final DeliveryTaskRepository taskRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final DeliveryTaskService deliveryTaskService;
    private final OsrmClient osrmClient;

    @Value("${fleet.fuel-efficiency.diesel-km-per-liter:8.0}")
    private double dieselKmPerLiter;

    @Value("${fleet.fuel-efficiency.petrol-km-per-liter:10.0}")
    private double petrolKmPerLiter;

    public RouteOptimizationService(RouteRepository routeRepository,
                                    DeliveryTaskRepository taskRepository,
                                    VehicleRepository vehicleRepository,
                                    DriverRepository driverRepository,
                                    DeliveryTaskService deliveryTaskService,
                                    OsrmClient osrmClient) {
        this.routeRepository = routeRepository;
        this.taskRepository = taskRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.deliveryTaskService = deliveryTaskService;
        this.osrmClient = osrmClient;
    }

    public List<RouteResponse> listAll() {
        return routeRepository.findAll().stream().map(this::toResponse).toList();
    }

    public RouteResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public RouteResponse optimizeRoute(RouteOptimizationRequest request) {
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

        // Determine depot coordinates (Default Bengaluru coordinates if not provided)
        double depotLat = request.startLatitude() != null ? request.startLatitude() : 12.9716;
        double depotLng = request.startLongitude() != null ? request.startLongitude() : 77.5946;

        // Build coordinates: [depot, task1, task2, ...]
        // OSRM expects [longitude, latitude]
        List<double[]> coordinates = new ArrayList<>();
        coordinates.add(new double[]{depotLng, depotLat});
        for (DeliveryTask task : tasks) {
            coordinates.add(new double[]{task.getLongitude(), task.getLatitude()});
        }

        // Fetch distance matrix from OSRM
        Map<String, double[][]> matrix = osrmClient.getDistanceMatrix(coordinates);
        double[][] distanceMatrix = matrix.get("distances");

        // Apply Nearest Neighbor heuristic (starting from depot = index 0)
        int n = coordinates.size();
        int[] order = nearestNeighbor(distanceMatrix, n);

        // Apply 2-opt improvement
        order = twoOptImprove(distanceMatrix, order);

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

        route = routeRepository.save(route);

        // Assign tasks to route in optimized order
        for (int i = 1; i < order.length; i++) {
            int taskIndex = order[i] - 1; // -1 because depot is at index 0
            DeliveryTask task = tasks.get(taskIndex);
            task.assignToRoute(route, i);
            taskRepository.save(task);
        }

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

    // ---- TSP Heuristic Implementations ----

    private int[] nearestNeighbor(double[][] distMatrix, int n) {
        boolean[] visited = new boolean[n];
        int[] order = new int[n];
        order[0] = 0;
        visited[0] = true;

        for (int step = 1; step < n; step++) {
            int current = order[step - 1];
            int nearest = -1;
            double nearestDist = Double.MAX_VALUE;

            for (int j = 0; j < n; j++) {
                if (!visited[j] && distMatrix[current][j] < nearestDist) {
                    nearestDist = distMatrix[current][j];
                    nearest = j;
                }
            }

            if (nearest == -1) {
                // Fallback: pick any unvisited
                for (int j = 0; j < n; j++) {
                    if (!visited[j]) {
                        nearest = j;
                        break;
                    }
                }
            }

            order[step] = nearest;
            visited[nearest] = true;
        }

        return order;
    }

    private int[] twoOptImprove(double[][] distMatrix, int[] order) {
        int n = order.length;
        if (n <= 3) {
            return order;
        }

        int[] improved = Arrays.copyOf(order, n);
        boolean madeImprovement = true;

        while (madeImprovement) {
            madeImprovement = false;
            for (int i = 1; i < n - 1; i++) {
                for (int j = i + 1; j < n; j++) {
                    double currentDist = distMatrix[improved[i - 1]][improved[i]]
                            + distMatrix[improved[j]][improved[(j + 1) % n]];
                    double newDist = distMatrix[improved[i - 1]][improved[j]]
                            + distMatrix[improved[i]][improved[(j + 1) % n]];

                    if (newDist < currentDist - 0.01) {
                        reverse(improved, i, j);
                        madeImprovement = true;
                    }
                }
            }
        }

        return improved;
    }

    private void reverse(int[] arr, int from, int to) {
        while (from < to) {
            int temp = arr[from];
            arr[from] = arr[to];
            arr[to] = temp;
            from++;
            to--;
        }
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
