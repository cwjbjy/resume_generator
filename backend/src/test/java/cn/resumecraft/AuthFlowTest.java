package cn.resumecraft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cn.resumecraft.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

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
    u.nickname = "资料映射测试";
    u.email = "profile@example.com";
    u.avatarUrl = "/avatars/profile.png";
    u.memberExpireAt = LocalDateTime.now().plusDays(1);
    users.updateById(u);
    assertThat(u.passwordHash).startsWith("$2a$");
    assertThat(passwords.matches("test123456", u.passwordHash)).isTrue();
    call(
        "POST",
        "/api/auth/register",
        null,
        Map.of("username", u.username, "password", "test123456"),
        409);
    JsonNode login =
        call(
            "POST",
            "/api/auth/login",
            null,
            Map.of("username", u.username, "password", "test123456"),
            200);
    assertThat(login.size()).isEqualTo(3);
    assertThat(login.path("token").asText()).isNotBlank();
    assertThat(login.path("id").asLong()).isEqualTo(a.id());
    assertThat(login.path("username").asText()).isEqualTo(u.username);
    Account loggedIn = new Account(login.path("id").asLong(), login.path("token").asText());
    JsonNode current = call("GET", "/api/users/me", loggedIn, null, 200);
    assertThat(current.path("id").asLong()).isEqualTo(a.id());
    assertThat(current.path("username").asText()).isEqualTo(u.username);
    assertThat(current.has("passwordHash")).isFalse();
    assertThat(current.has("password")).isFalse();
    JsonNode expectedProfile = json.readTree(json.writeValueAsBytes(users.selectById(a.id())));
    assertThat(current).isEqualTo(expectedProfile);
    call(
        "POST",
        "/api/auth/login",
        null,
        Map.of("username", u.username, "password", "test123456"),
        200);
    call("POST", "/api/auth/register", null, Map.of("username", "bad", "password", "x"), 400);
  }

  @Test
  void currentProfileUsesAuthenticatedIdentity() throws Exception {
    Account owner = account();
    Account another = account();
    User ownerUser = users.selectById(owner.id());

    call("GET", "/api/users/me", null, null, 401);
    JsonNode current = call("GET", "/api/users/me", owner, null, 200);
    assertThat(current.path("id").asLong()).isEqualTo(owner.id());
    assertThat(current.path("username").asText()).isEqualTo(ownerUser.username);
    JsonNode withAnotherId = call("GET", "/api/users/me?id=" + another.id(), owner, null, 200);
    assertThat(withAnotherId).isEqualTo(current);
  }

  @Test
  void loginHidesWhetherUsernameExists() throws Exception {
    Account account = account();
    User user = users.selectById(account.id());
    String unknownUsername =
        "missing" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

    for (String username : new String[] {user.username, unknownUsername}) {
      mvc.perform(
              post("/api/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      json.writeValueAsBytes(
                          Map.of("username", username, "password", "incorrect"))))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.code").value(401))
          .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }
  }
}
