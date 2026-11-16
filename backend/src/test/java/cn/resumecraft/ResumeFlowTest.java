package cn.resumecraft;

import static org.assertj.core.api.Assertions.assertThat;

import cn.resumecraft.entity.Order;
import cn.resumecraft.entity.User;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ResumeFlowTest extends ApiTestSupport {
  @Test
  void resumesArePrivateAndVipIsCheckedOnServer() throws Exception {
    Account owner = account(), other = account();
    JsonNode r = resume(owner, freeTemplateId());
    long id = r.path("id").asLong();
    for (String method : List.of("GET", "DELETE"))
      call(method, "/api/resumes/" + id, other, null, 403);
    call("POST", "/api/resumes/" + id + "/duplicate", other, null, 403);
    call("PUT", "/api/resumes/" + id, other, saveBody(r), 403);
    call("POST", "/api/resumes", owner, Map.of("templateId", vipTemplateId()), 403);
    ObjectNode body = saveBody(r);
    body.put("templateId", vipTemplateId());
    call("PUT", "/api/resumes/" + id, owner, body, 403);
  }

  @Test
  void optimisticVersionPreventsOverwriteAndContentIsValidated() throws Exception {
    Account a = account();
    JsonNode r = resume(a, freeTemplateId());
    String path = "/api/resumes/" + r.path("id").asLong();
    ObjectNode body = saveBody(r);
    body.put("title", "更新后的标题");
    JsonNode updated = call("PUT", path, a, body, 200);
    assertThat(updated.path("content").isObject()).isTrue();
    assertThat(updated.has("status")).isFalse();
    assertThat(updated.has("createdAt")).isFalse();
    assertThat(updated.path("version").asInt()).isEqualTo(1);
    call("PUT", path, a, body, 409);
    body.put("version", 1);
    ((ObjectNode) body.path("content").path("edu").get(0)).put("end", "2010-01");
    call("PUT", path, a, body, 400);
    JsonNode copy = call("POST", path + "/duplicate", a, null, 200);
    assertThat(copy.path("id")).isNotEqualTo(r.path("id"));
    call("DELETE", path, a, null, 200);
    call("GET", path, a, null, 404);
  }

  @Test
  void invalidContentDoesNotReplaceTheStoredResume() throws Exception {
    Account owner = account();
    JsonNode original = resume(owner, freeTemplateId());
    String path = "/api/resumes/" + original.path("id").asLong();
    ObjectNode missingName = saveBody(original);
    ((ObjectNode) missingName.path("content").path("basic")).remove("name");
    call("PUT", path, owner, missingName, 400);
    ObjectNode nullEntry = saveBody(original);
    ((ObjectNode) nullEntry.path("content")).withArray("edu").addNull();
    call("PUT", path, owner, nullEntry, 400);
    JsonNode stored = call("GET", path, owner, null, 200);
    assertThat(stored.path("version").asInt()).isZero();
    assertThat(stored.path("content")).isEqualTo(original.path("content"));
  }

  @Test
  void resumeListIsPaginatedAndContainsOnlySummaries() throws Exception {
    Account owner = account();
    Account other = account();
    long firstId = resume(owner, freeTemplateId()).path("id").asLong();
    long secondId = resume(owner, freeTemplateId()).path("id").asLong();
    long thirdId = resume(owner, freeTemplateId()).path("id").asLong();
    resume(other, freeTemplateId());

    JsonNode firstPage = call("GET", "/api/resumes?page=1&size=2", owner, null, 200);
    assertThat(firstPage.path("total").asLong()).isEqualTo(3);
    assertThat(firstPage.path("page").asInt()).isEqualTo(1);
    assertThat(firstPage.path("size").asInt()).isEqualTo(2);
    assertThat(firstPage.path("records").size()).isEqualTo(2);
    assertThat(firstPage.path("records").get(0).path("id").asLong()).isEqualTo(thirdId);
    assertThat(firstPage.path("records").get(1).path("id").asLong()).isEqualTo(secondId);
    JsonNode summary = firstPage.path("records").get(0);
    assertThat(summary.path("basic").isObject()).isTrue();
    assertThat(summary.path("basic").path("name").asText()).isNotBlank();
    assertThat(summary.has("content")).isFalse();
    assertThat(summary.has("userId")).isFalse();

    JsonNode lastPage = call("GET", "/api/resumes?page=2&size=2", owner, null, 200);
    assertThat(lastPage.path("records").size()).isEqualTo(1);
    assertThat(lastPage.path("records").get(0).path("id").asLong()).isEqualTo(firstId);
    assertThat(
            call("GET", "/api/resumes?page=3&size=2", owner, null, 200).path("records").isEmpty())
        .isTrue();
    call("GET", "/api/resumes?page=0", owner, null, 400);
    call("GET", "/api/resumes?size=101", owner, null, 400);
  }

  @Test
  void expiredMemberMustSwitchToFreeTemplate() throws Exception {
    Account a = account();
    Order o = order(a, planId(30));
    var c = callback(o);
    payments.callback(c, signatureService.sign(c));
    JsonNode r = resume(a, vipTemplateId());
    users.update(
        null,
        new LambdaUpdateWrapper<User>()
            .eq(User::getId, a.id())
            .set(User::getMemberExpireAt, LocalDateTime.now().minusDays(1)));
    call("POST", "/api/exports", a, Map.of("resumeId", r.path("id").asLong()), 403);
    ObjectNode body = saveBody(r);
    body.put("templateId", freeTemplateId());
    call("PUT", "/api/resumes/" + r.path("id").asLong(), a, body, 200);
  }
}
