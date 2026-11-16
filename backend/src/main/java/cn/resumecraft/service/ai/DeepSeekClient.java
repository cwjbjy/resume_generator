package cn.resumecraft.service.ai;

import static cn.resumecraft.exception.BizException.require;

import cn.resumecraft.config.AiProperties;
import cn.resumecraft.exception.BizException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DeepSeekClient {
  private final AiProperties properties;
  private final ObjectMapper json;
  private final HttpClient http;

  public DeepSeekClient(AiProperties properties, ObjectMapper json) {
    this.properties = properties;
    this.json = json;
    this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
  }

  public boolean available() {
    return properties.getApiKey() != null && !properties.getApiKey().isBlank();
  }

  public JsonNode complete(String instructions, Object input) {
    require(available(), 503, "AI 服务尚未配置，请设置 DEEPSEEK_API_KEY 后重启后端");
    try {
      String payload =
          json.writeValueAsString(
              Map.of(
                  "model",
                  properties.getModel(),
                  "messages",
                  List.of(
                      Map.of("role", "system", "content", instructions),
                      Map.of("role", "user", "content", json.writeValueAsString(input))),
                  "response_format",
                  Map.of("type", "json_object"),
                  "stream",
                  false,
                  "max_tokens",
                  8192,
                  "temperature",
                  0.3));
      URI endpoint =
          URI.create(properties.getBaseUrl().replaceAll("/+$", "") + "/chat/completions");
      HttpRequest request =
          HttpRequest.newBuilder(endpoint)
              .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
              .header("Authorization", "Bearer " + properties.getApiKey())
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(payload))
              .build();
      HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
      require(
          response.statusCode() != 401 && response.statusCode() != 402,
          503,
          "AI 服务认证或余额异常，请检查 DeepSeek 配置");
      require(response.statusCode() != 429, 503, "AI 服务繁忙，请稍后重试");
      require(response.statusCode() >= 200 && response.statusCode() < 300, 502, "AI 服务暂时不可用，请稍后重试");
      require(response.body().length() <= 600000, 502, "AI 响应内容过大，请缩小生成范围后重试");
      JsonNode envelope = json.readTree(response.body());
      require(envelope != null && envelope.isObject(), 502, "AI 返回的数据格式不正确，请重试");
      JsonNode choice = envelope.path("choices").path(0);
      require("stop".equals(choice.path("finish_reason").asText()), 502, "AI 响应未完整生成，请缩小内容范围后重试");
      JsonNode content = choice.path("message").path("content");
      require(content.isTextual() && !content.textValue().isBlank(), 502, "AI 返回了空内容，请重试");
      JsonNode result = json.readTree(content.textValue());
      require(result != null && result.isObject(), 502, "AI 返回的数据格式不正确，请重试");
      return result;
    } catch (HttpTimeoutException exception) {
      throw new BizException(504, "AI 处理超时，请稍后重试");
    } catch (JsonProcessingException exception) {
      throw new BizException(502, "AI 返回的数据格式不正确，请重试");
    } catch (IOException exception) {
      throw new BizException(502, "暂时无法连接 AI 服务，请稍后重试");
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new BizException(503, "AI 请求已中断，请重试");
    } catch (IllegalArgumentException exception) {
      throw new BizException(503, "AI 服务配置无效，请检查服务地址与密钥");
    }
  }
}
