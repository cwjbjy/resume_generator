package cn.resumecraft.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("resumes")
public class Resume {
  @TableId public Long id;
  public Long userId;
  public Long templateId;
  public String title;
  public String content;
  public Integer version;
  public String status;
  public LocalDateTime createdAt;
  public LocalDateTime updatedAt;

  public Long getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
  }

  public Long getTemplateId() {
    return templateId;
  }

  public String getTitle() {
    return title;
  }

  public String getContent() {
    return content;
  }

  public Integer getVersion() {
    return version;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
