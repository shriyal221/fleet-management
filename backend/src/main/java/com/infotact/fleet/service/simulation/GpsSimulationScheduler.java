package com.infotact.fleet.service.simulation;

import com.infotact.fleet.api.dto.DeliveryStatusUpdateRequest;
import com.infotact.fleet.domain.*;
import com.infotact.fleet.repository.*;
import com.infotact.fleet.service.DeliveryTaskService;
import com.infotact.fleet.service.RouteOptimizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;

@Component
@EnableScheduling
public class GpsSimulationScheduler {

    private static final Logger log = LoggerFactory.getLogger(GpsSimulationScheduler.class);

    private final RouteRepository routeRepository;
    private final DeliveryTaskRepository taskRepository;
    private final VehicleRepository vehicleRepository;
    private final DeliveryTaskService taskService;
    private final RouteOptimizationService routeService;
    private final SimpMessagingTemplate messagingTemplate;

    public GpsSimulationScheduler(RouteRepository routeRepository,
                                  DeliveryTaskRepository taskRepository,
                                  VehicleRepository vehicleRepository,
                                  DeliveryTaskService taskService,
                                  RouteOptimizationService routeService,
                                  SimpMessagingTemplate messagingTemplate) {
        this.routeRepository = routeRepository;
        this.taskRepository = taskRepository;
        this.vehicleRepository = vehicleRepository;
        this.taskService = taskService;
        this.routeService = routeService;
        this.messagingTemplate = messagingTemplate;
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void runSimulation() {
        List<Route> activeRoutes = routeRepository.findAllByStatus(RouteStatus.ACTIVE);
        if (activeRoutes.isEmpty()) {
            return;
        }

        log.debug("Simulating GPS for {} active routes...", activeRoutes.size());

        for (Route route : activeRoutes) {
            try {
                simulateRouteStep(route);
            } catch (Exception e) {
                log.error("Error simulating GPS step for route ID {}: {}", route.getId(), e.getMessage(), e);
            }
        }
    }

    private void simulateRouteStep(Route route) {
        Vehicle vehicle = route.getVehicle();
        if (vehicle == null) return;

        // Current coordinates
        double currentLat = vehicle.getCurrentLatitude() != null ? vehicle.getCurrentLatitude() : route.getStartLatitude();
        double currentLng = vehicle.getCurrentLongitude() != null ? vehicle.getCurrentLongitude() : route.getStartLongitude();

        // Find route stops
        List<DeliveryTask> stops = taskRepository.findAllByRouteId(route.getId());
        stops.sort(Comparator.comparingInt(t -> t.getSequenceIndex() != null ? t.getSequenceIndex() : 0));

        // Find first incomplete stop
        DeliveryTask targetStop = null;
        for (DeliveryTask stop : stops) {
            if (stop.getDeliveryStatus() == DeliveryStatus.DISPATCHED || stop.getDeliveryStatus() == DeliveryStatus.IN_TRANSIT) {
                targetStop = stop;
                break;
            }
        }

        double targetLat;
        double targetLng;
        String targetLabel;

        if (targetStop != null) {
            targetLat = targetStop.getLatitude();
            targetLng = targetStop.getLongitude();
            targetLabel = "Stop ID: " + targetStop.getId() + " (" + targetStop.getDeliveryAddress() + ")";

            // If the vehicle was just dispatched, change task status to IN_TRANSIT on first movement
            if (targetStop.getDeliveryStatus() == DeliveryStatus.DISPATCHED) {
                taskService.updateStatus(targetStop.getId(), new DeliveryStatusUpdateRequest("IN_TRANSIT"));
            }
        } else {
            // All stops completed - return to depot
            targetLat = route.getStartLatitude();
            targetLng = route.getStartLongitude();
            targetLabel = "Depot";
        }

        // Calculate delta
        double dLat = targetLat - currentLat;
        double dLng = targetLng - currentLng;
        double distance = Math.sqrt(dLat * dLat + dLng * dLng);

        // Movement Step Size (~200 meters in coordinates)
        double stepSize = 0.002;

        if (distance <= stepSize) {
            // Arrived at destination!
            vehicle.updateLocation(targetLat, targetLng);
            vehicleRepository.save(vehicle);

            if (targetStop != null) {
                // Arrived at stop - mark DELIVERED
                log.info("Vehicle {} arrived at {}", vehicle.getLicensePlate(), targetLabel);
                taskService.updateStatus(targetStop.getId(), new DeliveryStatusUpdateRequest("DELIVERED"));
            } else {
                // Returned to depot - complete route!
                log.info("Vehicle {} returned to depot. Completing route {}", vehicle.getLicensePlate(), route.getRouteName());
                routeService.completeRoute(route.getId());
            }
        } else {
            // Move step-by-step
            double newLat = currentLat + (dLat / distance) * stepSize;
            double newLng = currentLng + (dLng / distance) * stepSize;
            vehicle.updateLocation(newLat, newLng);
            vehicleRepository.save(vehicle);
        }

        // Broadcast current GPS location to clients over WebSockets
        GpsUpdatePayload payload = new GpsUpdatePayload(
                route.getId(),
                route.getRouteName(),
                vehicle.getId(),
                vehicle.getLicensePlate(),
                vehicle.getCurrentLatitude(),
                vehicle.getCurrentLongitude(),
                targetLabel,
                route.getStatus().name()
        );

        messagingTemplate.convertAndSend("/topic/gps", payload);
    }

    public record GpsUpdatePayload(
            Long routeId,
            String routeName,
            Long vehicleId,
            String vehiclePlate,
            Double latitude,
            Double longitude,
            String currentTarget,
            String routeStatus
    ) {}
}
