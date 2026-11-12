package cn.resumecraft.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("export_logs")
public class ExportLog {
  @TableId public Long id;
  public Long userId;
  public Long resumeId;
  public Long templateId;
  public String status;
  public LocalDateTime createdAt;

  public Long getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
  }

  public String getStatus() {
    return status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
