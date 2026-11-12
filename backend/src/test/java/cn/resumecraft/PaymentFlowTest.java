package cn.resumecraft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cn.resumecraft.dto.PaymentCallback;
import cn.resumecraft.model.Order;
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
import org.springframework.http.MediaType;

class PaymentFlowTest extends ApiTestSupport {
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
