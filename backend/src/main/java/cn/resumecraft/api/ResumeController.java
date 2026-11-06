package cn.resumecraft.api;

import cn.resumecraft.dto.CreateResumeRequest;
import cn.resumecraft.dto.SaveResumeRequest;
import cn.resumecraft.model.Resume;
import cn.resumecraft.service.ResumeService;
import jakarta.validation.Valid;
import java.util.List;
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
public class ResumeController {
  private final ResumeService resumeService;

  public ResumeController(ResumeService resumeService) {
    this.resumeService = resumeService;
  }

  @PostMapping
  Api<Resume> create(
      Authentication authentication, @Valid @RequestBody CreateResumeRequest request) {
    return Api.ok(resumeService.create(userId(authentication), request.templateId()));
  }

  @GetMapping
  Api<List<Resume>> list(Authentication authentication) {
    return Api.ok(resumeService.list(userId(authentication)));
  }

  @GetMapping("/{id}")
  Api<Resume> get(Authentication authentication, @PathVariable Long id) {
    return Api.ok(resumeService.owned(userId(authentication), id));
  }

  @PutMapping("/{id}")
  Api<Resume> save(
      Authentication authentication,
      @PathVariable Long id,
      @Valid @RequestBody SaveResumeRequest request) {
    return Api.ok(resumeService.save(userId(authentication), id, request));
  }

  @DeleteMapping("/{id}")
  Api<Void> delete(Authentication authentication, @PathVariable Long id) {
    resumeService.delete(userId(authentication), id);
    return Api.ok(null);
  }

  @PostMapping("/{id}/duplicate")
  Api<Resume> duplicate(Authentication authentication, @PathVariable Long id) {
    return Api.ok(resumeService.duplicate(userId(authentication), id));
  }

  private Long userId(Authentication authentication) {
    return (Long) authentication.getPrincipal();
  }
}
