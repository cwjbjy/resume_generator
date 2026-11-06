package cn.resumecraft.dto;

import jakarta.validation.constraints.NotNull;

public record ReserveExportRequest(@NotNull Long resumeId) {}
