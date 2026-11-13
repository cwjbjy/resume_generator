package cn.resumecraft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cn.resumecraft.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuthFlowTest extends ApiTestSupport {
  @Test
  void healthAndAuthenticationBoundary() throws Exception {
    mvc.perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
    call("GET", "/api/resumes", null, null, 401);
    call("GET", "/api/templates", null, null, 200);
    mvc.perform(get("/api/resumes").header("Authorization", "Bearer broken-token"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void registrationUsesBcryptAndUniqueUsername() throws Exception {
    Account a = account();
    User u = users.selectById(a.id());
    assertThat(u.passwordHash).startsWith("$2a$");
    assertThat(passwords.matches("test123456", u.passwordHash)).isTrue();
    call(
        "POST",
        "/api/auth/register",
        null,
        Map.of("username", u.username, "password", "test123456"),
        409);
    call(
        "POST",
        "/api/auth/login",
        null,
        Map.of("username", u.username, "password", "incorrect"),
        401);
    JsonNode login =
        call(
            "POST",
            "/api/auth/login",
            null,
            Map.of("username", u.username, "password", "test123456"),
            200);
    assertThat(login.path("user").has("passwordHash")).isFalse();
    call("POST", "/api/auth/register", null, Map.of("username", "bad", "password", "x"), 400);
  }
}
