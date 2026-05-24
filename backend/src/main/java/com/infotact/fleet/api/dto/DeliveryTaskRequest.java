package com.infotact.fleet.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record DeliveryTaskRequest(
    @NotBlank(message = "Delivery address is required")
    String deliveryAddress,
    
    String recipientName,
    String recipientPhone,
    
    @NotNull(message = "Latitude is required")
    Double latitude,
    
    @NotNull(message = "Longitude is required")
    Double longitude,
    
    Double packageWeightKg,
    Double packageVolumeCbm,
    
    Instant timeWindowStart,
    Instant timeWindowEnd,
    String notes
) {}
