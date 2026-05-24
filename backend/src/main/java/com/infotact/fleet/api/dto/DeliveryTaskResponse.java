package com.infotact.fleet.api.dto;

import java.time.Instant;

public record DeliveryTaskResponse(
    Long id,
    String deliveryAddress,
    String recipientName,
    String recipientPhone,
    Double latitude,
    Double longitude,
    Double packageWeightKg,
    Double packageVolumeCbm,
    String deliveryStatus,
    Instant timeWindowStart,
    Instant timeWindowEnd,
    Instant actualDeliveryTime,
    String notes,
    Integer sequenceIndex,
    Long routeId,
    String routeName,
    Instant createdAt
) {}
