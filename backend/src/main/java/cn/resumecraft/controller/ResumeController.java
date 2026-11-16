package cn.resumecraft.controller;

import cn.resumecraft.common.ApiResponse;
import cn.resumecraft.dto.CreateResumeRequest;
import cn.resumecraft.dto.SaveResumeRequest;
import cn.resumecraft.entity.Resume;
import cn.resumecraft.security.LoginUser;
import cn.resumecraft.service.ResumeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {
  private final ResumeService resumeService;

  @PostMapping
  ApiResponse<Resume> create(
      @AuthenticationPrincipal LoginUser loginUser,
      @Valid @RequestBody CreateResumeRequest request) {
    return ApiResponse.ok(resumeService.create(loginUser.userId(), request.templateId()));
  }

  @GetMapping
  ApiResponse<List<Resume>> list(@AuthenticationPrincipal LoginUser loginUser) {
    return ApiResponse.ok(resumeService.list(loginUser.userId()));
  }

  @GetMapping("/{id}")
  ApiResponse<Resume> get(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
    return ApiResponse.ok(resumeService.owned(loginUser.userId(), id));
  }

  @PutMapping("/{id}")
  ApiResponse<Resume> save(
      @AuthenticationPrincipal LoginUser loginUser,
      @PathVariable Long id,
      @Valid @RequestBody SaveResumeRequest request) {
    return ApiResponse.ok(resumeService.save(loginUser.userId(), id, request));
  }

  @DeleteMapping("/{id}")
  ApiResponse<Void> delete(@AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
    resumeService.delete(loginUser.userId(), id);
    return ApiResponse.ok(null);
  }

  @PostMapping("/{id}/duplicate")
  ApiResponse<Resume> duplicate(
      @AuthenticationPrincipal LoginUser loginUser, @PathVariable Long id) {
    return ApiResponse.ok(resumeService.duplicate(loginUser.userId(), id));
  }
}
