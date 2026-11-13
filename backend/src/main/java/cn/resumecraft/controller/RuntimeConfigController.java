package cn.resumecraft.controller;

import cn.resumecraft.common.ApiResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RuntimeConfigController {
  private final Environment environment;

  @GetMapping("/api/config")
  ApiResponse<Map<String, Boolean>> config() {
    return ApiResponse.ok(Map.of("mockEnabled", environment.matchesProfiles("dev")));
  }
}
