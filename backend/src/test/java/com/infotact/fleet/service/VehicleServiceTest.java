package com.infotact.fleet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.infotact.fleet.api.dto.VehicleRequest;
import com.infotact.fleet.api.dto.VehicleResponse;
import com.infotact.fleet.domain.Vehicle;
import com.infotact.fleet.repository.VehicleRepository;
import java.time.Year;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private AuditService auditService;

    private VehicleService service;

    @BeforeEach
    void setUp() {
        service = new VehicleService(vehicleRepository, auditService);
    }

    @Test
    void createRejectsFutureModelYear() {
        int invalidFutureYear = Year.now().getValue() + 3;
        VehicleRequest request = new VehicleRequest(
                "KA-01-AB-1234",
                "Tata",
                "Ace",
                invalidFutureYear,
                800.0,
                5.0,
                "DIESEL"
        );

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Vehicle year cannot be later than");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void createVehicleSuccessfullyWhenInputsAreValid() {
        VehicleRequest request = new VehicleRequest(
                "KA-01-AB-1234",
                "Tata",
                "Ace",
                2024,
                800.0,
                5.0,
                "DIESEL"
        );

        when(vehicleRepository.existsByLicensePlate("KA-01-AB-1234")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle vehicle = invocation.getArgument(0);
            ReflectionTestUtils.setField(vehicle, "id", 100L);
            return vehicle;
        });

        VehicleResponse response = service.create(request);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.licensePlate()).isEqualTo("KA-01-AB-1234");
        assertThat(response.maintenanceStatus()).isEqualTo("OPERATIONAL");
        verify(auditService).log(anyString(), anyString());
    }

    @Test
    void updateMaintenanceStatusSuccessfully() {
        Vehicle vehicle = new Vehicle("KA-01-AB-1234", "Tata", "Ace", 2024, 800.0, 5.0, "DIESEL");
        ReflectionTestUtils.setField(vehicle, "id", 15L);

        when(vehicleRepository.findById(15L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        VehicleResponse response = service.updateStatus(15L, "IN_MAINTENANCE");

        assertThat(response.maintenanceStatus()).isEqualTo("IN_MAINTENANCE");
        verify(auditService).log(anyString(), anyString());
    }
}
