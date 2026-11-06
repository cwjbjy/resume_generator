package cn.resumecraft.service;

import static cn.resumecraft.api.BizException.require;

import cn.resumecraft.mapper.ExportLogMapper;
import cn.resumecraft.mapper.UserMapper;
import cn.resumecraft.model.ExportLog;
import cn.resumecraft.model.Resume;
import cn.resumecraft.model.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExportService {
  private final ExportLogMapper exportLogMapper;
  private final UserMapper userMapper;
  private final ResumeService resumeService;
  private final TemplateService templateService;
  private final StringRedisTemplate redis;
  private final boolean redisEnabled;

  public ExportService(
      ExportLogMapper exportLogMapper,
      UserMapper userMapper,
      ResumeService resumeService,
      TemplateService templateService,
      StringRedisTemplate redis,
      @Value("${app.redis-enabled:false}") boolean redisEnabled) {
    this.exportLogMapper = exportLogMapper;
    this.userMapper = userMapper;
    this.resumeService = resumeService;
    this.templateService = templateService;
    this.redis = redis;
    this.redisEnabled = redisEnabled;
  }

  @Transactional
  public ExportLog reserve(Long userId, Long resumeId) {
    User user = userMapper.lock(userId);
    Resume resume = resumeService.owned(userId, resumeId);
    templateService.checkAccess(userId, resume.templateId);
    releaseExpiredReservations(userId);
    if (!user.isMember()) {
      checkDailyQuota(userId);
    }
    checkRepeatedClick(userId);
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
          new UpdateWrapper<ExportLog>()
              .eq("id", exportId)
              .eq("status", "PENDING")
              .set("status", success ? "COMPLETED" : "FAILED"));
    }
    return exportLogMapper.selectById(exportId);
  }

  public List<ExportLog> history(Long userId) {
    return exportLogMapper.selectList(
        new QueryWrapper<ExportLog>()
            .eq("user_id", userId)
            .orderByDesc("created_at")
            .last("LIMIT 100"));
  }

  private void releaseExpiredReservations(Long userId) {
    exportLogMapper.update(
        null,
        new UpdateWrapper<ExportLog>()
            .eq("user_id", userId)
            .eq("status", "PENDING")
            .lt("created_at", LocalDateTime.now().minusMinutes(10))
            .set("status", "FAILED"));
  }

  private void checkDailyQuota(Long userId) {
    long used =
        exportLogMapper.selectCount(
            new QueryWrapper<ExportLog>()
                .eq("user_id", userId)
                .ge("created_at", LocalDate.now().atStartOfDay())
                .in("status", "PENDING", "COMPLETED"));
    require(used < 1, 403, "今日免费导出次数已用完，明天 00:00 重置；会员可不限次数导出");
  }

  private void checkRepeatedClick(Long userId) {
    if (!redisEnabled) {
      return;
    }
    try {
      Boolean acquired =
          redis.opsForValue().setIfAbsent("export:burst:" + userId, "1", Duration.ofSeconds(2));
      require(Boolean.TRUE.equals(acquired), 429, "操作太快，请稍后重试");
    } catch (RedisConnectionFailureException unavailable) {
      // Daily quota remains guarded by the database transaction and user row lock.
    }
  }
}
