package com.infotact.fleet.api.dto;

import java.time.Instant;

public record VehicleResponse(
    Long id,
    String licensePlate,
    String make,
    String model,
    Integer year,
    Double capacityKg,
    Double capacityVolumeCbm,
    String fuelType,
    Double currentOdometerKm,
    String maintenanceStatus,
    Instant lastMaintenanceDate,
    Instant nextMaintenanceDueDate,
    Double currentLatitude,
    Double currentLongitude,
    Instant createdAt
) {}
