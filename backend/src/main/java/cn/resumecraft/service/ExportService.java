package cn.resumecraft.service;

import static cn.resumecraft.exception.BizException.require;

import cn.resumecraft.dto.ExportResponse;
import cn.resumecraft.entity.ExportLog;
import cn.resumecraft.entity.Resume;
import cn.resumecraft.entity.User;
import cn.resumecraft.mapper.ExportLogMapper;
import cn.resumecraft.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExportService {
  private final ExportLogMapper exportLogMapper;
  private final UserMapper userMapper;
  private final ResumeService resumeService;
  private final TemplateService templateService;

  @Transactional
  public ExportLog reserve(Long userId, Long resumeId) {
    User user = userMapper.lock(userId);
    Resume resume = resumeService.owned(userId, resumeId);
    templateService.checkAccess(userId, resume.templateId);
    releaseExpiredReservations(userId);
    checkPendingExport(userId);
    if (!user.isMember()) {
      checkDailyQuota(userId);
    }
    ExportLog log = new ExportLog();
    log.userId = userId;
    log.resumeId = resumeId;
    log.templateId = resume.templateId;
    log.status = "PENDING";
    log.createdAt = LocalDateTime.now();
    exportLogMapper.insert(log);
    return log;
  }

  @Transactional
  public ExportLog finish(Long userId, Long exportId, boolean success) {
    userMapper.lock(userId);
    ExportLog log = exportLogMapper.selectById(exportId);
    require(log != null && log.userId.equals(userId), 404, "导出任务不存在");
    if (log.status.equals("PENDING")) {
      require(
          !success || log.createdAt.isAfter(LocalDateTime.now().minusMinutes(10)),
          409,
          "导出任务已超时，请重新生成");
      exportLogMapper.update(
          null,
          new LambdaUpdateWrapper<ExportLog>()
              .eq(ExportLog::getId, exportId)
              .eq(ExportLog::getStatus, "PENDING")
              .set(ExportLog::getStatus, success ? "COMPLETED" : "FAILED"));
    }
    return exportLogMapper.selectById(exportId);
  }

  public IPage<ExportResponse> history(Long userId, int page, int size) {
    return exportLogMapper
        .selectPage(
            new Page<>(page, size),
            new LambdaQueryWrapper<ExportLog>()
                .eq(ExportLog::getUserId, userId)
                .orderByDesc(ExportLog::getCreatedAt)
                .orderByDesc(ExportLog::getId))
        .convert(ExportResponse::from);
  }

  private void releaseExpiredReservations(Long userId) {
    exportLogMapper.update(
        null,
        new LambdaUpdateWrapper<ExportLog>()
            .eq(ExportLog::getUserId, userId)
            .eq(ExportLog::getStatus, "PENDING")
            .lt(ExportLog::getCreatedAt, LocalDateTime.now().minusMinutes(10))
            .set(ExportLog::getStatus, "FAILED"));
  }

  private void checkDailyQuota(Long userId) {
    long used =
        exportLogMapper.selectCount(
            new LambdaQueryWrapper<ExportLog>()
                .eq(ExportLog::getUserId, userId)
                .ge(ExportLog::getCreatedAt, LocalDate.now().atStartOfDay())
                .in(ExportLog::getStatus, "PENDING", "COMPLETED"));
    require(used < 1, 403, "今日免费导出次数已用完，明天 00:00 重置；会员可不限次数导出");
  }

  private void checkPendingExport(Long userId) {
    long pending =
        exportLogMapper.selectCount(
            new LambdaQueryWrapper<ExportLog>()
                .eq(ExportLog::getUserId, userId)
                .eq(ExportLog::getStatus, "PENDING"));
    require(pending == 0, 409, "已有导出任务正在生成，请稍后重试");
  }
}
