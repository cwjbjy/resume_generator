package cn.resumecraft.controller;

import cn.resumecraft.common.ApiResponse;
import cn.resumecraft.dto.ResumeResponse;
import cn.resumecraft.dto.ai.AiChatRequest;
import cn.resumecraft.dto.ai.AiChatResponse;
import cn.resumecraft.dto.ai.AiCreateResumeRequest;
import cn.resumecraft.dto.ai.AiExportReviewRequest;
import cn.resumecraft.dto.ai.AiMatchRequest;
import cn.resumecraft.dto.ai.AiMatchResponse;
import cn.resumecraft.dto.ai.AiPolishRequest;
import cn.resumecraft.dto.ai.AiPolishResponse;
import cn.resumecraft.dto.ai.AiReviewResponse;
import cn.resumecraft.dto.ai.AiStatusResponse;
import cn.resumecraft.security.LoginUser;
import cn.resumecraft.service.ResumeService;
import cn.resumecraft.service.ai.AiExportReviewService;
import cn.resumecraft.service.ai.AiResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
  private final AiResumeService aiResumeService;
  private final AiExportReviewService exportReviewService;
  private final ResumeService resumeService;

  @GetMapping("/status")
  ApiResponse<AiStatusResponse> status() {
    return ApiResponse.ok(aiResumeService.status());
  }

  @PostMapping("/polish")
  ApiResponse<AiPolishResponse> polish(
      @AuthenticationPrincipal LoginUser loginUser, @Valid @RequestBody AiPolishRequest request) {
    return ApiResponse.ok(aiResumeService.polish(loginUser.userId(), request));
  }

  @PostMapping("/chat")
  ApiResponse<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
    return ApiResponse.ok(aiResumeService.chat(request));
  }

  @PostMapping("/resumes")
  ApiResponse<ResumeResponse> create(
      @AuthenticationPrincipal LoginUser loginUser,
      @Valid @RequestBody AiCreateResumeRequest request) {
    return ApiResponse.ok(
        resumeService.response(
            resumeService.createFromContent(
                loginUser.userId(), request.templateId(), request.title(), request.content())));
  }

  @PostMapping("/match")
  ApiResponse<AiMatchResponse> match(
      @AuthenticationPrincipal LoginUser loginUser, @Valid @RequestBody AiMatchRequest request) {
    return ApiResponse.ok(aiResumeService.match(loginUser.userId(), request));
  }

  @PostMapping("/export-review")
  ApiResponse<AiReviewResponse> review(
      @AuthenticationPrincipal LoginUser loginUser,
      @Valid @RequestBody AiExportReviewRequest request) {
    return ApiResponse.ok(exportReviewService.review(loginUser.userId(), request.exportId()));
  }
}
