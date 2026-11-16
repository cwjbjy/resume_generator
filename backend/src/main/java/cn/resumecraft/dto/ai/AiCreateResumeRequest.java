package cn.resumecraft.dto.ai;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AiCreateResumeRequest(
    @NotNull @Positive Long templateId,
    @NotBlank @Size(max = 100) String title,
    @NotNull JsonNode content) {}
