package cn.resumecraft;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.ai.api-key=")
class AiUnavailableFlowTest extends ApiTestSupport {
  @Test
  void missingKeyKeepsStatusAvailableAndOtherBusinessUsable() throws Exception {
    Account owner = account();
    var resume = resume(owner, freeTemplateId());
    var status = call("GET", "/api/ai/status", owner, null, 200);
    assertThat(status.path("available").asBoolean()).isFalse();
    assertThat(status.path("model").asText()).isNotBlank();
    assertThat(status.has("apiKey")).isFalse();
    call(
        "POST",
        "/api/ai/polish",
        owner,
        Map.of("resumeId", resume.path("id").asLong(), "module", "basic"),
        503);
    call(
        "POST",
        "/api/ai/chat",
        owner,
        Map.of("messages", List.of(Map.of("role", "user", "content", "我想生成简历"))),
        503);
    call("GET", "/api/users/me", owner, null, 200);
    call("GET", "/api/ai/status", null, null, 401);
    call(
        "POST",
        "/api/ai/chat",
        null,
        Map.of("messages", List.of(Map.of("role", "user", "content", "生成简历"))),
        401);
  }
}
