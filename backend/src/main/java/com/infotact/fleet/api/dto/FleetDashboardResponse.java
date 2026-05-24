package com.infotact.fleet.api.dto;

public record FleetDashboardResponse(
    long totalVehicles,
    long operationalVehicles,
    long maintenanceVehicles,
    long totalDrivers,
    long availableDrivers,
    long activeDrivers,
    long totalDeliveries,
    long unassignedDeliveries,
    long inTransitDeliveries,
    long completedDeliveries,
    long activeRoutes,
    long plannedRoutes,
    long completedRoutes
) {}
