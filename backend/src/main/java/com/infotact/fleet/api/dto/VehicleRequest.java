package com.infotact.fleet.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record VehicleRequest(
    @NotBlank(message = "License plate is required")
    @Size(max = 30, message = "License plate must be 30 characters or fewer")
    String licensePlate,
    
    @NotBlank(message = "Make is required")
    @Size(max = 50, message = "Make must be 50 characters or fewer")
    String make,
    
    @NotBlank(message = "Model is required")
    @Size(max = 50, message = "Model must be 50 characters or fewer")
    String model,
    
    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Invalid year")
    Integer year,
    
    @NotNull(message = "Payload capacity is required")
    @DecimalMin(value = "0.01", message = "Capacity must be greater than zero")
    Double capacityKg,
    
    @PositiveOrZero(message = "Volume capacity cannot be negative")
    Double capacityVolumeCbm,
    
    @NotBlank(message = "Fuel type is required")
    @Pattern(regexp = "DIESEL|PETROL|CNG|ELECTRIC", message = "Fuel type must be DIESEL, PETROL, CNG, or ELECTRIC")
    String fuelType
) {}
