package cn.resumecraft;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cn.resumecraft.dto.PaymentCallback;
import cn.resumecraft.mapper.ExportLogMapper;
import cn.resumecraft.mapper.OrderMapper;
import cn.resumecraft.mapper.UserMapper;
import cn.resumecraft.model.Order;
import cn.resumecraft.service.ExportService;
import cn.resumecraft.service.OrderService;
import cn.resumecraft.service.PaymentService;
import cn.resumecraft.service.PaymentSignatureService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@Import(MySqlTestConfiguration.class)
@ActiveProfiles({"dev", "test"})
abstract class ApiTestSupport {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired UserMapper users;
  @Autowired OrderMapper orders;
  @Autowired ExportLogMapper exports;
  @Autowired PaymentService payments;
  @Autowired OrderService orderService;
  @Autowired PaymentSignatureService signatureService;
  @Autowired ExportService exportService;
  @Autowired PasswordEncoder passwords;

  record Account(long id, String token) {}

  Account account() throws Exception {
    String name = "test" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
    JsonNode data =
        call(
            "POST",
            "/api/auth/register",
            null,
            Map.of("username", name, "password", "test123456"),
            200);
    return new Account(data.path("user").path("id").asLong(), data.path("token").asText());
  }

  JsonNode call(String method, String path, Account user, Object body, int status)
      throws Exception {
    var req =
        switch (method) {
          case "POST" -> post(path);
          case "PUT" -> put(path);
          case "DELETE" -> delete(path);
          default -> get(path);
        };
    req.contentType(MediaType.APPLICATION_JSON);
    if (user != null) req.header("Authorization", "Bearer " + user.token());
    if (body != null) req.content(json.writeValueAsBytes(body));
    MvcResult result = mvc.perform(req).andExpect(status().is(status)).andReturn();
    return json.readTree(result.getResponse().getContentAsByteArray()).path("data");
  }

  JsonNode resume(Account a, long template) throws Exception {
    return call("POST", "/api/resumes", a, Map.of("templateId", template), 200);
  }

  Order order(Account a, int plan) {
    return orderService.create(a.id(), (long) plan, UUID.randomUUID().toString());
  }

  PaymentCallback callback(Order o) {
    return new PaymentCallback(
        o.orderNo, o.amount, "TX_" + o.orderNo, "SUCCESS", Instant.now().getEpochSecond());
  }

  ObjectNode saveBody(JsonNode r) throws Exception {
    ObjectNode b = json.createObjectNode();
    b.put("title", r.path("title").asText());
    b.put("templateId", r.path("templateId").asLong());
    b.put("version", r.path("version").asInt());
    b.set("content", json.readTree(r.path("content").asText()));
    return b;
  }
}
