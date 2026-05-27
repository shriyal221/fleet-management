package com.infotact.fleet.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalTime;

public record DriverRequest(
    @NotBlank(message = "Driver name is required")
    @Size(max = 100, message = "Driver name must be 100 characters or fewer")
    String name,
    
    @Pattern(regexp = "^(\\+?[0-9]{10,15})?$", message = "Contact number must contain 10 to 15 digits")
    String contactNumber,

    @Email(message = "Email must be valid")
    String email,
    
    @NotBlank(message = "License number is required")
    @Size(max = 50, message = "License number must be 50 characters or fewer")
    String licenseNumber,
    
    @NotNull(message = "License expiry date is required")
    Instant licenseExpiry,
    
    LocalTime shiftStart,
    LocalTime shiftEnd
) {}
