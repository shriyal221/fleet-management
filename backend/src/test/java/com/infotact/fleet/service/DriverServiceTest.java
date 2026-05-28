package com.infotact.fleet.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.infotact.fleet.api.dto.DriverRequest;
import com.infotact.fleet.domain.Driver;
import com.infotact.fleet.repository.DriverRepository;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private VehicleService vehicleService;

    @Test
    void createRejectsExpiredLicense() {
        DriverService service = new DriverService(driverRepository, vehicleService);
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
}
