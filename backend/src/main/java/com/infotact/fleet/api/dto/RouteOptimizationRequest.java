package com.infotact.fleet.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public record RouteOptimizationRequest(
    @NotEmpty(message = "At least one delivery task is required")
    List<Long> deliveryTaskIds,
    
    @NotNull(message = "Vehicle ID is required")
    Long vehicleId,
    
    @NotNull(message = "Driver ID is required")
    Long driverId,
    
    @DecimalMin(value = "-90.0", message = "Start latitude must be at least -90")
    @DecimalMax(value = "90.0", message = "Start latitude must be at most 90")
    Double startLatitude,

    @DecimalMin(value = "-180.0", message = "Start longitude must be at least -180")
    @DecimalMax(value = "180.0", message = "Start longitude must be at most 180")
    Double startLongitude,

    Instant plannedDepartureTime
) {}
