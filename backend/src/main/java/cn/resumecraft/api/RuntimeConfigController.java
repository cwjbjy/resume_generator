package cn.resumecraft.api;

import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RuntimeConfigController {
  private final Environment environment;

  public RuntimeConfigController(Environment environment) {
    this.environment = environment;
  }

  @GetMapping("/api/config")
  Api<Map<String, Boolean>> config() {
    return Api.ok(Map.of("mockEnabled", environment.matchesProfiles("dev")));
  }
}
