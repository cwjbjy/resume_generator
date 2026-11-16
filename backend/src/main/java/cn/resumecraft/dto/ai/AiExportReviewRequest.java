package cn.resumecraft.dto.ai;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AiExportReviewRequest(@NotNull @Positive Long exportId) {}
