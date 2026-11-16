package cn.resumecraft.dto;

import cn.resumecraft.entity.Resume;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

public record ResumeSummaryResponse(
    Long id,
    Long templateId,
    String title,
    Integer version,
    LocalDateTime updatedAt,
    JsonNode basic) {
  public static ResumeSummaryResponse from(Resume resume, JsonNode basic) {
    return new ResumeSummaryResponse(
        resume.id, resume.templateId, resume.title, resume.version, resume.updatedAt, basic);
  }
}
