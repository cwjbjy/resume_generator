package cn.resumecraft.dto;

import cn.resumecraft.entity.ExportLog;
import java.time.LocalDateTime;

public record ExportResponse(Long id, Long resumeId, LocalDateTime createdAt, String status) {
  public static ExportResponse from(ExportLog log) {
    return new ExportResponse(log.id, log.resumeId, log.createdAt, log.status);
  }
}
