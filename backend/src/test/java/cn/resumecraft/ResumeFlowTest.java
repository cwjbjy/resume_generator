package cn.resumecraft;

import static org.assertj.core.api.Assertions.assertThat;

import cn.resumecraft.model.Order;
import cn.resumecraft.model.User;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
  void expiredMemberMustSwitchToFreeTemplate() throws Exception {
    Account a = account();
    Order o = order(a, planId(30));
    var c = callback(o);
    payments.callback(c, signatureService.sign(c));
    JsonNode r = resume(a, vipTemplateId());
    users.update(
        null,
        new UpdateWrapper<User>()
            .eq("id", a.id())
            .set("member_expire_at", LocalDateTime.now().minusDays(1)));
    call("POST", "/api/exports", a, Map.of("resumeId", r.path("id").asLong()), 403);
    ObjectNode body = saveBody(r);
    body.put("templateId", freeTemplateId());
    call("PUT", "/api/resumes/" + r.path("id").asLong(), a, body, 200);
  }
}
