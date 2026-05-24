package com.infotact.fleet.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RouteOptimizationRequest(
    @NotEmpty(message = "At least one delivery task is required")
    List<Long> deliveryTaskIds,
    
    @NotNull(message = "Vehicle ID is required")
    Long vehicleId,
    
    @NotNull(message = "Driver ID is required")
    Long driverId,
    
    Double startLatitude,
    Double startLongitude
) {}
