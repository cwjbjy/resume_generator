package cn.resumecraft.controller;

import cn.resumecraft.common.ApiResponse;
import cn.resumecraft.dto.CompleteExportRequest;
import cn.resumecraft.dto.ExportResponse;
import cn.resumecraft.dto.PageResponse;
import cn.resumecraft.dto.ReserveExportRequest;
import cn.resumecraft.security.LoginUser;
import cn.resumecraft.service.ExportService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exports")
@RequiredArgsConstructor
public class ExportController {
  private final ExportService exportService;

  @PostMapping
  ApiResponse<ExportResponse> reserve(
      @AuthenticationPrincipal LoginUser loginUser,
      @Valid @RequestBody ReserveExportRequest request) {
    return ApiResponse.ok(
        ExportResponse.from(exportService.reserve(loginUser.userId(), request.resumeId())));
  }

  @PostMapping("/{id}/complete")
  ApiResponse<ExportResponse> complete(
      @AuthenticationPrincipal LoginUser loginUser,
      @PathVariable Long id,
      @RequestBody CompleteExportRequest request) {
    return ApiResponse.ok(
        ExportResponse.from(exportService.finish(loginUser.userId(), id, request.success())));
  }

  @GetMapping("/logs")
  ApiResponse<PageResponse<ExportResponse>> logs(
      @AuthenticationPrincipal LoginUser loginUser,
      @RequestParam(defaultValue = "1") @Min(1) int page,
      @RequestParam(defaultValue = "12") @Min(1) @Max(100) int size) {
    return ApiResponse.ok(PageResponse.from(exportService.history(loginUser.userId(), page, size)));
  }
}
