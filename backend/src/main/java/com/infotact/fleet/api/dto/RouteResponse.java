package com.infotact.fleet.api.dto;

import java.time.Instant;
import java.util.List;

public record RouteResponse(
    Long id,
    String routeName,
    Long vehicleId,
    String vehiclePlate,
    Long driverId,
    String driverName,
    String status,
    Double totalDistanceKm,
    Integer estimatedDurationMinutes,
    Double totalFuelEstimateLiters,
    Double startLatitude,
    Double startLongitude,
    Integer stopCount,
    List<DeliveryTaskResponse> deliveryStops,
    Instant dispatchedAt,
    Instant completedAt,
    Instant createdAt
) {}
