package cn.resumecraft.controller;

import cn.resumecraft.common.ApiResponse;
import cn.resumecraft.dto.CreateResumeRequest;
import cn.resumecraft.dto.SaveResumeRequest;
import cn.resumecraft.entity.Resume;
import cn.resumecraft.service.ResumeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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
      Authentication authentication, @Valid @RequestBody CreateResumeRequest request) {
    return ApiResponse.ok(resumeService.create(userId(authentication), request.templateId()));
  }

  @GetMapping
  ApiResponse<List<Resume>> list(Authentication authentication) {
    return ApiResponse.ok(resumeService.list(userId(authentication)));
  }

  @GetMapping("/{id}")
  ApiResponse<Resume> get(Authentication authentication, @PathVariable Long id) {
    return ApiResponse.ok(resumeService.owned(userId(authentication), id));
  }

  @PutMapping("/{id}")
  ApiResponse<Resume> save(
      Authentication authentication,
      @PathVariable Long id,
      @Valid @RequestBody SaveResumeRequest request) {
    return ApiResponse.ok(resumeService.save(userId(authentication), id, request));
  }

  @DeleteMapping("/{id}")
  ApiResponse<Void> delete(Authentication authentication, @PathVariable Long id) {
    resumeService.delete(userId(authentication), id);
    return ApiResponse.ok(null);
  }

  @PostMapping("/{id}/duplicate")
  ApiResponse<Resume> duplicate(Authentication authentication, @PathVariable Long id) {
    return ApiResponse.ok(resumeService.duplicate(userId(authentication), id));
  }

  private Long userId(Authentication authentication) {
    return (Long) authentication.getPrincipal();
  }
}
