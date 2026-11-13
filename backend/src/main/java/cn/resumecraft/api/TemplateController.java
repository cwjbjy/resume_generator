package cn.resumecraft.api;

import cn.resumecraft.model.Template;
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
  Api<List<Template>> list(@RequestParam(defaultValue = "all") String filter) {
    return Api.ok(templateService.list(filter));
  }

  @GetMapping("/{id}")
  Api<Template> get(@PathVariable Long id) {
    return Api.ok(templateService.get(id));
  }
}
