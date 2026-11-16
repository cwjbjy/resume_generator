package cn.resumecraft.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AiMatchRequest(
    @NotNull @Positive Long resumeId, @NotBlank @Size(max = 12000) String jobDescription) {}
