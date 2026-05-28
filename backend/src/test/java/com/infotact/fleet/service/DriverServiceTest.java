package com.infotact.fleet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.infotact.fleet.api.dto.DriverRequest;
import com.infotact.fleet.api.dto.DriverResponse;
import com.infotact.fleet.domain.Driver;
import com.infotact.fleet.domain.Vehicle;
import com.infotact.fleet.domain.VehicleMaintenanceStatus;
import com.infotact.fleet.repository.DriverRepository;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private VehicleService vehicleService;

    @Mock
    private AuditService auditService;

    private DriverService service;

    @BeforeEach
    void setUp() {
        service = new DriverService(driverRepository, vehicleService, auditService);
    }

    @Test
    void createRejectsExpiredLicense() {
        DriverRequest request = new DriverRequest(
                "Expired Driver",
                "+919876543210",
                "driver@example.com",
                "KA-DL-OLD",
                Instant.now().minus(1, ChronoUnit.DAYS),
                LocalTime.of(8, 0),
                LocalTime.of(18, 0)
        );

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("license expiry");

        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    void createDriverSuccessfullyWhenInputsAreValid() {
        DriverRequest request = new DriverRequest(
                "Active Driver",
                "+919876543210",
                "driver@example.com",
                "KA-DL-NEW",
                Instant.now().plus(30, ChronoUnit.DAYS),
                LocalTime.of(8, 0),
                LocalTime.of(18, 0)
        );

        when(driverRepository.existsByLicenseNumber("KA-DL-NEW")).thenReturn(false);
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> {
            Driver driver = invocation.getArgument(0);
            ReflectionTestUtils.setField(driver, "id", 100L);
            return driver;
        });

        DriverResponse response = service.create(request);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.name()).isEqualTo("Active Driver");
        assertThat(response.status()).isEqualTo("AVAILABLE");
        verify(auditService).log(anyString(), anyString());
    }

    @Test
    void assignVehicleThrowsErrorWhenVehicleIsNotOperational() {
        Driver driver = new Driver("Active Driver", null, null, "KA-DL-VALID", Instant.now().plus(30, ChronoUnit.DAYS), null, null);
        ReflectionTestUtils.setField(driver, "id", 1L);
        
        Vehicle vehicle = new Vehicle("KA-01-AB-1234", "Tata", "Ace", 2024, 800.0, 5.0, "DIESEL");
        vehicle.updateMaintenanceStatus(VehicleMaintenanceStatus.IN_MAINTENANCE);
        ReflectionTestUtils.setField(vehicle, "id", 2L);

        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(vehicleService.findOrThrow(2L)).thenReturn(vehicle);

        assertThatThrownBy(() -> service.assignVehicle(1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only OPERATIONAL vehicles");
    }
}
