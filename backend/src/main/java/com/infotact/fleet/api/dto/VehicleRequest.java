package com.infotact.fleet.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehicleRequest(
    @NotBlank(message = "License plate is required")
    String licensePlate,
    
    @NotBlank(message = "Make is required")
    String make,
    
    @NotBlank(message = "Model is required")
    String model,
    
    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Invalid year")
    Integer year,
    
    @NotNull(message = "Payload capacity is required")
    @Min(value = 0, message = "Capacity must be positive")
    Double capacityKg,
    
    Double capacityVolumeCbm,
    
    @NotBlank(message = "Fuel type is required")
    String fuelType
) {}
