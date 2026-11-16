package cn.resumecraft.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReserveExportRequest(
    @NotNull @Positive Long resumeId, @Min(0) Integer resumeVersion) {}
