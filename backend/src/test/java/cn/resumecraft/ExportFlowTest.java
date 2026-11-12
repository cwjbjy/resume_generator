package cn.resumecraft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cn.resumecraft.api.BizException;
import cn.resumecraft.model.ExportLog;
import cn.resumecraft.model.Order;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        if (value > 0) successes.add(value);
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
        new LambdaUpdateWrapper<ExportLog>()
            .eq(ExportLog::getId, pending.id)
            .set(ExportLog::getCreatedAt, LocalDateTime.now().minusMinutes(11)));
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

  @Test
  void memberCanHaveOnlyOnePendingExportAcrossResumes() throws Exception {
    Account member = account();
    Order membershipOrder = order(member, planId(30));
    var membershipCallback = callback(membershipOrder);
    payments.callback(membershipCallback, signatureService.sign(membershipCallback));
    long firstResumeId = resume(member, freeTemplateId()).path("id").asLong();
    long secondResumeId = resume(member, freeTemplateId()).path("id").asLong();
    List<Long> acceptedExports = new ArrayList<>();
    ExecutorService pool = Executors.newFixedThreadPool(4);
    try {
      List<Callable<Long>> requests = new ArrayList<>();
      for (int request = 0; request < 8; request++) {
        requests.add(
            () -> {
              try {
                return exportService.reserve(member.id(), firstResumeId).id;
              } catch (BizException conflict) {
                assertThat(conflict.status).isEqualTo(409);
                return -1L;
              }
            });
      }
      for (Future<Long> result : pool.invokeAll(requests)) {
        long exportId = result.get();
        if (exportId > 0) acceptedExports.add(exportId);
      }
    } finally {
      pool.shutdownNow();
    }
    assertThat(acceptedExports).hasSize(1);
    call("POST", "/api/exports", member, Map.of("resumeId", secondResumeId), 409);
    exportService.finish(member.id(), acceptedExports.get(0), false);

    long nextExportId =
        call("POST", "/api/exports", member, Map.of("resumeId", secondResumeId), 200)
            .path("id")
            .asLong();
    assertThat(exportService.finish(member.id(), acceptedExports.get(0), true).status)
        .isEqualTo("FAILED");
    call("POST", "/api/exports", member, Map.of("resumeId", firstResumeId), 409);
    exportService.finish(member.id(), nextExportId, true);
    call("POST", "/api/exports", member, Map.of("resumeId", firstResumeId), 200);
  }

  @Test
  void pendingExportDoesNotBlockAnotherUser() throws Exception {
    Account firstUser = account();
    Account secondUser = account();
    long firstResumeId = resume(firstUser, freeTemplateId()).path("id").asLong();
    long secondResumeId = resume(secondUser, freeTemplateId()).path("id").asLong();
    exportService.reserve(firstUser.id(), firstResumeId);
    call("POST", "/api/exports", secondUser, Map.of("resumeId", secondResumeId), 200);
  }
}
