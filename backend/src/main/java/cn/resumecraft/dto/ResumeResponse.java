package cn.resumecraft.dto;

import cn.resumecraft.entity.Resume;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

public record ResumeResponse(
    Long id,
    Long userId,
    Long templateId,
    String title,
    JsonNode content,
    Integer version,
    LocalDateTime updatedAt) {
  public static ResumeResponse from(Resume resume, JsonNode content) {
    return new ResumeResponse(
        resume.id,
        resume.userId,
        resume.templateId,
        resume.title,
        content,
        resume.version,
        resume.updatedAt);
  }
}
