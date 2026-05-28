package com.infotact.fleet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.infotact.fleet.api.dto.DeliveryTaskResponse;
import com.infotact.fleet.api.dto.RouteOptimizationRequest;
import com.infotact.fleet.api.dto.RouteResponse;
import com.infotact.fleet.domain.DeliveryTask;
import com.infotact.fleet.domain.Driver;
import com.infotact.fleet.domain.Route;
import com.infotact.fleet.domain.Vehicle;
import com.infotact.fleet.repository.DeliveryTaskRepository;
import com.infotact.fleet.repository.DriverRepository;
import com.infotact.fleet.repository.RouteRepository;
import com.infotact.fleet.repository.VehicleRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RouteOptimizationServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private DeliveryTaskRepository taskRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private DeliveryTaskService deliveryTaskService;

    @Mock
    private OsrmClient osrmClient;

    private RouteOptimizationService service;

    @BeforeEach
    void setUp() {
        service = new RouteOptimizationService(
                routeRepository,
                taskRepository,
                vehicleRepository,
                driverRepository,
                deliveryTaskService,
                osrmClient
        );
        ReflectionTestUtils.setField(service, "dieselKmPerLiter", 8.0);
        ReflectionTestUtils.setField(service, "petrolKmPerLiter", 10.0);
        ReflectionTestUtils.setField(service, "averageRouteSpeedKmh", 40.0);
        ReflectionTestUtils.setField(service, "maxWaypoints", 12);
    }

    @Test
    void optimizeRouteCreatesPlannedRouteWhenFleetRulesPass() {
        Instant departure = Instant.now().plus(1, ChronoUnit.HOURS);
        Vehicle vehicle = vehicle(1L, 500.0, 8.0);
        Driver driver = driver(2L, vehicle);
        DeliveryTask first = task(10L, 12.9716, 77.5946, 120.0, 1.0, departure.plus(3, ChronoUnit.HOURS));
        DeliveryTask second = task(11L, 12.9816, 77.6046, 80.0, 0.5, departure.plus(4, ChronoUnit.HOURS));

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(driverRepository.findById(2L)).thenReturn(Optional.of(driver));
        when(taskRepository.findById(10L)).thenReturn(Optional.of(first));
        when(taskRepository.findById(11L)).thenReturn(Optional.of(second));
        when(osrmClient.getDistanceMatrix(any())).thenReturn(Map.of("distances", new double[][]{
                {0.0, 1000.0, 2500.0},
                {1000.0, 0.0, 900.0},
                {2500.0, 900.0, 0.0}
        }));
        when(osrmClient.getRouteSummary(any())).thenReturn(Map.of("distance", 5600.0, "duration", 840.0));
        when(routeRepository.countByRouteNameStartingWith(anyString())).thenReturn(0L);
        when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> {
            Route route = invocation.getArgument(0);
            ReflectionTestUtils.setField(route, "id", 99L);
            return route;
        });
        when(taskRepository.findAllByRouteId(99L)).thenReturn(List.of(first, second));
        when(deliveryTaskService.toResponse(any(DeliveryTask.class))).thenAnswer(invocation -> {
            DeliveryTask task = invocation.getArgument(0);
            return new DeliveryTaskResponse(task.getId(), task.getDeliveryAddress(), null, null,
                    task.getLatitude(), task.getLongitude(), task.getPackageWeightKg(), task.getPackageVolumeCbm(),
                    task.getDeliveryStatus().name(), task.getTimeWindowStart(), task.getTimeWindowEnd(), null,
                    null, task.getSequenceIndex(), 99L, "RT-test", null);
        });

        RouteResponse response = service.optimizeRoute(new RouteOptimizationRequest(
                List.of(10L, 11L),
                1L,
                2L,
                12.9,
                77.5,
                departure
        ));

        assertThat(response.status()).isEqualTo("PLANNED");
        assertThat(response.vehiclePlate()).isEqualTo("KA-01-AB-1234");
        assertThat(response.stopCount()).isEqualTo(2);
        assertThat(response.totalDistanceKm()).isEqualTo(5.6);
        verify(taskRepository).save(first);
        verify(taskRepository).save(second);
    }

    @Test
    void optimizeRouteRejectsCapacityOverloadBeforeCallingOsrm() {
        Instant departure = Instant.now().plus(1, ChronoUnit.HOURS);
        Vehicle vehicle = vehicle(1L, 100.0, 2.0);
        Driver driver = driver(2L, vehicle);
        DeliveryTask task = task(10L, 12.9716, 77.5946, 125.0, 1.0, departure.plus(3, ChronoUnit.HOURS));

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(driverRepository.findById(2L)).thenReturn(Optional.of(driver));
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> service.optimizeRoute(new RouteOptimizationRequest(
                List.of(10L),
                1L,
                2L,
                null,
                null,
                departure
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeding vehicle capacity");

        verify(osrmClient, never()).getDistanceMatrix(any());
    }

    @Test
    void optimizeRouteRejectsStopsThatMissDeliveryWindow() {
        Instant departure = Instant.now().plus(1, ChronoUnit.HOURS);
        Vehicle vehicle = vehicle(1L, 500.0, 8.0);
        Driver driver = driver(2L, vehicle);
        DeliveryTask task = task(10L, 12.9716, 77.5946, 50.0, 1.0, departure.plus(2, ChronoUnit.MINUTES));

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(driverRepository.findById(2L)).thenReturn(Optional.of(driver));
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(osrmClient.getDistanceMatrix(any())).thenReturn(Map.of("distances", new double[][]{
                {0.0, 100000.0},
                {100000.0, 0.0}
        }));

        assertThatThrownBy(() -> service.optimizeRoute(new RouteOptimizationRequest(
                List.of(10L),
                1L,
                2L,
                null,
                null,
                departure
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("delivery time window closes");

        verify(osrmClient, never()).getRouteSummary(any());
        verify(routeRepository, never()).save(any(Route.class));
    }

    private Vehicle vehicle(Long id, Double capacityKg, Double capacityVolumeCbm) {
        Vehicle vehicle = new Vehicle("KA-01-AB-1234", "Tata", "Ace Gold", 2024, capacityKg, capacityVolumeCbm, "DIESEL");
        ReflectionTestUtils.setField(vehicle, "id", id);
        return vehicle;
    }

    private Driver driver(Long id, Vehicle assignedVehicle) {
        Driver driver = new Driver("Ramesh Kumar", "+919876543210", "ramesh@example.com",
                "KA-DL-2020-00101", Instant.now().plus(365, ChronoUnit.DAYS), null, null);
        driver.assignVehicle(assignedVehicle);
        ReflectionTestUtils.setField(driver, "id", id);
        return driver;
    }

    private DeliveryTask task(Long id, Double latitude, Double longitude, Double weightKg, Double volumeCbm, Instant windowEnd) {
        DeliveryTask task = new DeliveryTask("MG Road", "Customer", "+919876543211",
                latitude, longitude, weightKg, volumeCbm, null, windowEnd, "Test task");
        ReflectionTestUtils.setField(task, "id", id);
        return task;
    }
}
