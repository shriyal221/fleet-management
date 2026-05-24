package com.infotact.fleet.api.dto;

import jakarta.validation.constraints.NotBlank;

public record DeliveryStatusUpdateRequest(
    @NotBlank(message = "New delivery status is required")
    String status
) {}
