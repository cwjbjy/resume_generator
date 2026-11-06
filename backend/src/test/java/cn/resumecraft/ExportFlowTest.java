package cn.resumecraft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cn.resumecraft.model.ExportLog;
import cn.resumecraft.model.Order;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

class ExportFlowTest extends ApiTestSupport {
  @Test
  void exportReservationSerializesFreeQuotaAndFailureReleasesIt() throws Exception {
    Account a = account();
    long rid = resume(a, freeTemplateId()).path("id").asLong();
    ExecutorService pool = Executors.newFixedThreadPool(4);
    try {
      List<Callable<Long>> calls = new ArrayList<>();
      for (int i = 0; i < 5; i++)
        calls.add(
            () -> {
              try {
                return exportService.reserve(a.id(), rid).id;
              } catch (cn.resumecraft.api.BizException e) {
                return -1L;
              }
            });
      List<Long> successes = new ArrayList<>();
      for (Future<Long> f : pool.invokeAll(calls)) {
        Long value = f.get();
        if (value > 0)
          successes.add(value);
      }
      assertThat(successes).hasSize(1);
      exportService.finish(a.id(), successes.get(0), false);
    } finally {
      pool.shutdownNow();
    }
    ExportLog next = exportService.reserve(a.id(), rid);
    exportService.finish(a.id(), next.id, true);
    exportService.finish(a.id(), next.id, true);
    assertThat(exports.selectById(next.id).status).isEqualTo("COMPLETED");
    assertThatThrownBy(() -> exportService.reserve(a.id(), rid)).hasMessageContaining("次数已用完");
  }

  @Test
  void expiredReservationsReleaseQuotaAndMemberCanExportRepeatedly() throws Exception {
    Account a = account();
    long rid = resume(a, freeTemplateId()).path("id").asLong();
    ExportLog pending = exportService.reserve(a.id(), rid);
    exports.update(
        null,
        new UpdateWrapper<ExportLog>()
            .eq("id", pending.id)
            .set("created_at", LocalDateTime.now().minusMinutes(11)));
    ExportLog next = exportService.reserve(a.id(), rid);
    assertThat(exports.selectById(pending.id).status).isEqualTo("FAILED");
    exportService.finish(a.id(), next.id, true);
    Order o = order(a, planId(30));
    var c = callback(o);
    payments.callback(c, signatureService.sign(c));
    for (int i = 0; i < 3; i++) {
      ExportLog e = exportService.reserve(a.id(), rid);
      exportService.finish(a.id(), e.id, true);
    }
  }
}
