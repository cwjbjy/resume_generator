package cn.resumecraft;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

final class DeepSeekStub implements AutoCloseable {
  private record Reply(int status, String body, long delayMillis) {}

  private final ObjectMapper json = new ObjectMapper();
  private final HttpServer server;
  private final AtomicReference<Reply> reply = new AtomicReference<>(new Reply(500, "{}", 0));
  final List<JsonNode> requests = new CopyOnWriteArrayList<>();
  final List<String> authorizationHeaders = new CopyOnWriteArrayList<>();

  DeepSeekStub() throws IOException {
    server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
    server.createContext(
        "/chat/completions",
        exchange -> {
          requests.add(json.readTree(exchange.getRequestBody()));
          authorizationHeaders.add(exchange.getRequestHeaders().getFirst("Authorization"));
          Reply current = reply.get();
          try {
            Thread.sleep(current.delayMillis());
          } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
          }
          byte[] body = current.body().getBytes(StandardCharsets.UTF_8);
          try (exchange) {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(current.status(), body.length);
            exchange.getResponseBody().write(body);
          }
        });
    server.start();
  }

  String url() {
    return "http://localhost:" + server.getAddress().getPort();
  }

  void answer(Object result) throws IOException {
    answer(result, "stop");
  }

  void answer(Object result, String finishReason) throws IOException {
    raw(
        200,
        json.writeValueAsString(
            Map.of(
                "choices",
                List.of(
                    Map.of(
                        "finish_reason",
                        finishReason,
                        "message",
                        Map.of("content", json.writeValueAsString(result)))))));
  }

  void raw(int status, String body) {
    reply.set(new Reply(status, body, 0));
  }

  void delayed(long millis) {
    reply.set(new Reply(200, "{}", millis));
  }

  JsonNode lastRequest() {
    return requests.getLast();
  }

  @Override
  public void close() {
    server.stop(0);
  }
}
