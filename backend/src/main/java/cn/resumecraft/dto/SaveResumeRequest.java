package cn.resumecraft.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaveResumeRequest(
    @NotBlank(message = "简历标题不能为空") @Size(max = 100, message = "标题最多 100 字") String title,
    @NotNull Long templateId,
    @NotNull JsonNode content,
    @NotNull @Min(0) Integer version) {}
