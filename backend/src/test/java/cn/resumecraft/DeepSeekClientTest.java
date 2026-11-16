package cn.resumecraft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cn.resumecraft.config.AiProperties;
import cn.resumecraft.exception.BizException;
import cn.resumecraft.service.ai.DeepSeekClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DeepSeekClientTest {
  private final ObjectMapper json = new ObjectMapper();

  @Test
  void requestUsesServerCredentialsAndJsonMode() throws Exception {
    try (var stub = new DeepSeekStub()) {
      stub.answer(Map.of("reply", "请补充工作经历"));
      var client = client(stub);
      var result = client.complete("返回JSON", Map.of("message", "生成简历"));
      assertThat(result.path("reply").asText()).isEqualTo("请补充工作经历");
      assertThat(stub.authorizationHeaders).containsExactly("Bearer test-server-key");
      assertThat(stub.lastRequest().path("response_format").path("type").asText())
          .isEqualTo("json_object");
      assertThat(stub.lastRequest().path("model").asText()).isEqualTo("deepseek-flash");
      assertThat(stub.lastRequest().path("stream").asBoolean()).isFalse();
      assertThat(stub.lastRequest().path("messages").size()).isEqualTo(2);
    }
  }

  @Test
  void missingCredentialsDoNotSendARequest() throws Exception {
    try (var stub = new DeepSeekStub()) {
      AiProperties config = properties(stub);
      config.setApiKey("");
      DeepSeekClient client = new DeepSeekClient(config, json);
      assertThat(client.available()).isFalse();
      assertStatus(() -> client.complete("JSON", Map.of()), 503);
      assertThat(stub.requests).isEmpty();
    }
  }

  @Test
  void providerFailuresMalformedOrIncompleteResponsesAreMapped() throws Exception {
    try (var stub = new DeepSeekStub()) {
      var client = client(stub);
      for (int status : new int[] {401, 402, 429}) {
        stub.raw(status, "{\"error\":\"private upstream detail\"}");
        assertStatus(() -> client.complete("JSON", Map.of()), 503);
      }
      stub.raw(500, "private upstream detail");
      assertStatus(() -> client.complete("JSON", Map.of()), 502);
      for (String body :
          new String[] {
            "",
            "null",
            "[]",
            "not-json",
            "{}",
            "{\"choices\":[{\"finish_reason\":\"stop\",\"message\":{\"content\":\"   \"}}]}"
          }) {
        stub.raw(200, body);
        assertStatus(() -> client.complete("JSON", Map.of()), 502);
      }
      stub.answer(Map.of("reply", "truncated"), "length");
      assertStatus(() -> client.complete("JSON", Map.of()), 502);
      stub.answer("scalar");
      assertStatus(() -> client.complete("JSON", Map.of()), 502);
    }
  }

  @Test
  void requestTimeoutIsReportedAsGatewayTimeout() throws Exception {
    try (var stub = new DeepSeekStub()) {
      stub.delayed(1500);
      AiProperties config = properties(stub);
      config.setTimeoutSeconds(1);
      var client = new DeepSeekClient(config, json);
      assertStatus(() -> client.complete("JSON", Map.of()), 504);
    }
  }

  private DeepSeekClient client(DeepSeekStub stub) {
    return new DeepSeekClient(properties(stub), json);
  }

  private AiProperties properties(DeepSeekStub stub) {
    AiProperties config = new AiProperties();
    config.setBaseUrl(stub.url());
    config.setApiKey("test-server-key");
    return config;
  }

  private void assertStatus(Runnable call, int expected) {
    assertThatThrownBy(call::run)
        .isInstanceOfSatisfying(
            BizException.class,
            error -> {
              assertThat(error.status).isEqualTo(expected);
              assertThat(error.getMessage())
                  .doesNotContain("private upstream detail", "test-server-key");
            });
  }
}
