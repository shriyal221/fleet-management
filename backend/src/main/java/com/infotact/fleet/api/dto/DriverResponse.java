package com.infotact.fleet.api.dto;

import java.time.Instant;
import java.time.LocalTime;

public record DriverResponse(
    Long id,
    String name,
    String contactNumber,
    String email,
    String licenseNumber,
    Instant licenseExpiry,
    LocalTime shiftStart,
    LocalTime shiftEnd,
    String status,
    Long assignedVehicleId,
    String assignedVehiclePlate,
    boolean licenseValid,
    Instant createdAt
) {}
