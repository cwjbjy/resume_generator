package cn.resumecraft.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@TableName("export_logs")
public class ExportLog {
  @TableId public Long id;
  public Long userId;
  public Long resumeId;
  public Long templateId;
  public String status;
  public String contentSnapshot;
  public Integer resumeVersion;
  public LocalDateTime createdAt;
}
