package cn.resumecraft.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
    @NotNull Long planId, @NotBlank @Size(max = 64) String requestKey) {}
