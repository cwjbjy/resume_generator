package cn.resumecraft.controller;

import cn.resumecraft.common.ApiResponse;
import cn.resumecraft.entity.Template;
import cn.resumecraft.service.TemplateService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {
  private final TemplateService templateService;

  @GetMapping
  ApiResponse<List<Template>> list(@RequestParam(defaultValue = "all") String filter) {
    return ApiResponse.ok(templateService.list(filter));
  }

  @GetMapping("/{id}")
  ApiResponse<Template> get(@PathVariable Long id) {
    return ApiResponse.ok(templateService.get(id));
  }
}
