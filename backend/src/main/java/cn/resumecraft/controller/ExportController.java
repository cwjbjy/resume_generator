package cn.resumecraft.controller;

import cn.resumecraft.common.ApiResponse;
import cn.resumecraft.dto.CompleteExportRequest;
import cn.resumecraft.dto.ReserveExportRequest;
import cn.resumecraft.entity.ExportLog;
import cn.resumecraft.service.ExportService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exports")
@RequiredArgsConstructor
public class ExportController {
  private final ExportService exportService;

  @PostMapping
  ApiResponse<ExportLog> reserve(
      Authentication authentication, @Valid @RequestBody ReserveExportRequest request) {
    return ApiResponse.ok(
        exportService.reserve((Long) authentication.getPrincipal(), request.resumeId()));
  }

  @PostMapping("/{id}/complete")
  ApiResponse<ExportLog> complete(
      Authentication authentication,
      @PathVariable Long id,
      @RequestBody CompleteExportRequest request) {
    return ApiResponse.ok(
        exportService.finish((Long) authentication.getPrincipal(), id, request.success()));
  }

  @GetMapping("/logs")
  ApiResponse<List<ExportLog>> logs(Authentication authentication) {
    return ApiResponse.ok(exportService.history((Long) authentication.getPrincipal()));
  }
}
