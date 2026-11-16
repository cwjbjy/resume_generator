package cn.resumecraft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cn.resumecraft.dto.PaymentCallback;
import cn.resumecraft.entity.Order;
import cn.resumecraft.entity.Plan;
import cn.resumecraft.entity.User;
import cn.resumecraft.mapper.PlanMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class PaymentFlowTest extends ApiTestSupport {
  @Autowired PlanMapper plans;

  @Test
  void orderCreationIsIdempotentEvenWithConcurrentRequests() throws Exception {
    Account a = account();
    String key = UUID.randomUUID().toString();
    long plan = planId(30);
    ExecutorService pool = Executors.newFixedThreadPool(4);
    try {
      List<Callable<Long>> calls = new ArrayList<>();
      for (int i = 0; i < 8; i++) calls.add(() -> orderService.create(a.id(), plan, key).id);
      Set<Long> ids = new HashSet<>();
      for (Future<Long> f : pool.invokeAll(calls)) ids.add(f.get());
      assertThat(ids).hasSize(1);
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void idempotencyKeyCannotBeReusedForAnotherPlan() throws Exception {
    Account a = account();
    String key = UUID.randomUUID().toString();
    call("POST", "/api/orders", a, Map.of("planId", planId(30), "requestKey", key), 200);
    call("POST", "/api/orders", a, Map.of("planId", planId(365), "requestKey", key), 409);
  }

  @Test
  void paymentAndRefundUseTheOriginalPlanSnapshot() throws Exception {
    Account a = account();
    Plan plan = plans.selectById(planId(30));
    Order o = order(a, plan.id);
    LocalDateTime initialExpiry = LocalDateTime.now().withNano(0).plusDays(45);
    users.update(
        null,
        new LambdaUpdateWrapper<User>()
            .eq(User::getId, a.id())
            .set(User::getMemberExpireAt, initialExpiry));
    try {
      plans.update(
          null,
          new LambdaUpdateWrapper<Plan>()
              .eq(Plan::getId, plan.id)
              .set(Plan::getName, "调整后的套餐")
              .set(Plan::getDurationDays, 73)
              .set(Plan::getPrice, plan.price + 100));
      var detail = call("GET", "/api/orders/" + o.id, a, null, 200);
      assertThat(detail.path("planName").asText()).isEqualTo(plan.name);
      assertThat(detail.path("durationDays").asInt()).isEqualTo(plan.durationDays);
      assertThat(detail.path("amount").asInt()).isEqualTo(plan.price);
      var callback = callback(o);
      payments.callback(callback, signatureService.sign(callback));
      assertThat(users.selectById(a.id()).memberExpireAt)
          .isEqualTo(initialExpiry.plusDays(plan.durationDays));
      payments.refund(a.id(), o.id);
      assertThat(users.selectById(a.id()).memberExpireAt).isEqualTo(initialExpiry);
    } finally {
      plans.update(
          null,
          new LambdaUpdateWrapper<Plan>()
              .eq(Plan::getId, plan.id)
              .set(Plan::getName, plan.name)
              .set(Plan::getDurationDays, plan.durationDays)
              .set(Plan::getPrice, plan.price));
    }
  }

  @Test
  void readingAnExpiredOrderOnlyClosesThatOrder() throws Exception {
    Account a = account();
    long plan = planId(30);
    Order target = order(a, plan);
    Order other = order(a, plan);
    orders.update(
        null,
        new LambdaUpdateWrapper<Order>()
            .in(Order::getId, target.id, other.id)
            .set(Order::getExpiredAt, LocalDateTime.now().minusMinutes(1)));
    call("GET", "/api/orders/" + target.id, a, null, 200);
    assertThat(orders.selectById(target.id).status).isEqualTo("EXPIRED");
    assertThat(orders.selectById(other.id).status).isEqualTo("CREATED");
  }

  @Test
  void orderPagesExposeOnlyPublicFieldsAndPlansExposeFeatureArrays() throws Exception {
    Account a = account();
    long plan = planId(30);
    order(a, plan);
    Order middle = order(a, plan);
    Order newest = order(a, plan);
    var first = call("GET", "/api/orders?page=1&size=2", a, null, 200);
    assertThat(first.path("total").asLong()).isEqualTo(3);
    assertThat(first.path("page").asLong()).isEqualTo(1);
    assertThat(first.path("size").asLong()).isEqualTo(2);
    assertThat(first.path("records").size()).isEqualTo(2);
    assertThat(first.path("records").get(0).path("id").asLong()).isEqualTo(newest.id);
    assertThat(first.path("records").get(1).path("id").asLong()).isEqualTo(middle.id);
    for (var item : first.path("records")) {
      assertThat(item.hasNonNull("planName")).isTrue();
      assertThat(item.hasNonNull("durationDays")).isTrue();
      assertThat(item.has("userId")).isFalse();
      assertThat(item.has("requestKey")).isFalse();
      assertThat(item.has("transactionId")).isFalse();
      assertThat(item.has("channel")).isFalse();
    }
    var second = call("GET", "/api/orders?page=2&size=2", a, null, 200);
    assertThat(second.path("records").size()).isEqualTo(1);
    call("GET", "/api/orders?page=0", a, null, 400);
    call("GET", "/api/orders?size=101", a, null, 400);
    for (var item : call("GET", "/api/plans", null, null, 200)) {
      assertThat(item.path("features").isArray()).isTrue();
      assertThat(item.has("sort")).isFalse();
    }
  }

  @Test
  void replayedCallbacksGrantMembershipOnlyOnce() throws Exception {
    Account a = account();
    Order o = order(a, planId(30));
    var c = callback(o);
    String sig = signatureService.sign(c);
    ExecutorService pool = Executors.newFixedThreadPool(4);
    try {
      List<Callable<String>> calls = new ArrayList<>();
      for (int i = 0; i < 10; i++) calls.add(() -> payments.callback(c, sig));
      for (Future<String> f : pool.invokeAll(calls)) assertThat(f.get()).isEqualTo("success");
    } finally {
      pool.shutdownNow();
    }
    assertThat(orders.selectById(o.id).status).isEqualTo("PAID");
    LocalDateTime expiry = users.selectById(a.id()).memberExpireAt;
    assertThat(Duration.between(LocalDateTime.now(), expiry).toDays()).isBetween(29L, 30L);
    call("POST", "/api/resumes", a, Map.of("templateId", vipTemplateId()), 200);
  }

  @Test
  void signatureAmountExpiryAndOwnershipAreEnforced() throws Exception {
    Account a = account(), other = account();
    Order o = order(a, planId(30));
    var c = callback(o);
    mvc.perform(
            post("/api/pay/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Pay-Signature", "bad")
                .content(json.writeValueAsBytes(c)))
        .andExpect(status().isBadRequest());
    var tampered =
        new PaymentCallback(
            o.orderNo, 1, "wrong_amount", "SUCCESS", Instant.now().getEpochSecond());
    assertThatThrownBy(() -> payments.callback(tampered, signatureService.sign(tampered)))
        .hasMessage("支付金额不匹配");
    call("POST", "/api/pay/mock", other, Map.of("orderId", o.id, "success", true), 403);
    call("GET", "/api/orders/" + o.id, other, null, 403);
    orders.update(
        null,
        new LambdaUpdateWrapper<Order>()
            .eq(Order::getId, o.id)
            .set(Order::getExpiredAt, LocalDateTime.now().minusSeconds(1)));
    orderService.expire();
    assertThat(orders.selectById(o.id).status).isEqualTo("EXPIRED");
    assertThatThrownBy(() -> payments.callback(c, signatureService.sign(c)))
        .hasMessageContaining("已关闭");
    assertThat(users.selectById(a.id()).isMember()).isFalse();
  }

  @Test
  void failedPaymentCanRetryAndRefundIsIdempotent() throws Exception {
    Account a = account();
    Order first = order(a, planId(30));
    call("POST", "/api/pay/mock", a, Map.of("orderId", first.id, "success", false), 200);
    assertThat(users.selectById(a.id()).isMember()).isFalse();
    call("POST", "/api/pay/mock", a, Map.of("orderId", first.id, "success", true), 200);
    LocalDateTime original = users.selectById(a.id()).memberExpireAt;
    Order second = order(a, planId(365));
    var c = callback(second);
    payments.callback(c, signatureService.sign(c));
    assertThat(users.selectById(a.id()).memberExpireAt).isEqualTo(original.plusDays(365));
    payments.refund(a.id(), second.id);
    assertThat(users.selectById(a.id()).memberExpireAt).isEqualTo(original);
    assertThatThrownBy(() -> payments.refund(a.id(), second.id)).hasMessageContaining("只有已支付");
  }
}
