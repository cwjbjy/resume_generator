package cn.resumecraft.dto;

import jakarta.validation.constraints.NotNull;

public record CreateResumeRequest(@NotNull Long templateId) {}
