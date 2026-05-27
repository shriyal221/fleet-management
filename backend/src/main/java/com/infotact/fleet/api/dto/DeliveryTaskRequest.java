package com.infotact.fleet.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record DeliveryTaskRequest(
    @NotBlank(message = "Delivery address is required")
    @Size(max = 255, message = "Delivery address must be 255 characters or fewer")
    String deliveryAddress,
    
    @Size(max = 100, message = "Recipient name must be 100 characters or fewer")
    String recipientName,

    @Pattern(regexp = "^(\\+?[0-9]{10,15})?$", message = "Recipient phone must contain 10 to 15 digits")
    String recipientPhone,
    
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be at least -90")
    @DecimalMax(value = "90.0", message = "Latitude must be at most 90")
    Double latitude,
    
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be at least -180")
    @DecimalMax(value = "180.0", message = "Longitude must be at most 180")
    Double longitude,
    
    @PositiveOrZero(message = "Package weight cannot be negative")
    Double packageWeightKg,

    @PositiveOrZero(message = "Package volume cannot be negative")
    Double packageVolumeCbm,
    
    Instant timeWindowStart,
    Instant timeWindowEnd,
    String notes
) {}
