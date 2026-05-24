package com.infotact.fleet.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalTime;

public record DriverRequest(
    @NotBlank(message = "Driver name is required")
    String name,
    
    String contactNumber,
    String email,
    
    @NotBlank(message = "License number is required")
    String licenseNumber,
    
    @NotNull(message = "License expiry date is required")
    Instant licenseExpiry,
    
    LocalTime shiftStart,
    LocalTime shiftEnd
) {}
