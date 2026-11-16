package cn.resumecraft.controller;

import cn.resumecraft.common.ApiResponse;
import cn.resumecraft.dto.CreateResumeRequest;
import cn.resumecraft.dto.PageResponse;
import cn.resumecraft.dto.ResumeResponse;
import cn.resumecraft.dto.ResumeSummaryResponse;
import cn.resumecraft.dto.SaveResumeRequest;
import cn.resumecraft.security.LoginUser;
import cn.resumecraft.service.ResumeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {
  private final ResumeService resumeService;

  @PostMapping
  ApiResponse<ResumeResponse> create(
      @AuthenticationPrincipal LoginUser loginUser,
      @Valid @RequestBody CreateResumeRequest request) {
    return ApiResponse.ok(
        resumeService.response(resumeService.create(loginUser.userId(), request.templateId())));
  }

  @GetMapping
  ApiResponse<PageResponse<ResumeSummaryResponse>> list(
      @AuthenticationPrincipal LoginUser loginUser,
      @RequestParam(defaultValue = "1") @Min(1) int page,
      @RequestParam(defaultValue = "12") @Min(1) @Max(100) int size) {
    return ApiResponse.ok(PageResponse.from(resumeService.list(loginUser.userId(), page, size)));
  }

  @GetMapping("/{id}")
  ApiResponse<ResumeResponse> get(
      @AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
    return ApiResponse.ok(resumeService.response(resumeService.owned(loginUser.userId(), id)));
  }

  @PutMapping("/{id}")
  ApiResponse<ResumeResponse> save(
      @AuthenticationPrincipal LoginUser loginUser,
      @PathVariable Long id,
      @Valid @RequestBody SaveResumeRequest request) {
    return ApiResponse.ok(
        resumeService.response(resumeService.save(loginUser.userId(), id, request)));
  }

  @DeleteMapping("/{id}")
  ApiResponse<Void> delete(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
    resumeService.delete(loginUser.userId(), id);
    return ApiResponse.ok(null);
  }

  @PostMapping("/{id}/duplicate")
  ApiResponse<ResumeResponse> duplicate(
      @AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
    return ApiResponse.ok(resumeService.response(resumeService.duplicate(loginUser.userId(), id)));
  }
}
