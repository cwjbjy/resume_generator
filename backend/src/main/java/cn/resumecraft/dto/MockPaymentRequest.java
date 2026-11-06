package cn.resumecraft.dto;

import jakarta.validation.constraints.NotNull;

public record MockPaymentRequest(@NotNull Long orderId, boolean success) {}
