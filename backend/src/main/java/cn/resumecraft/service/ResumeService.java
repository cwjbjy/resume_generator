package cn.resumecraft.service;

import static cn.resumecraft.api.BizException.require;

import cn.resumecraft.dto.SaveResumeRequest;
import cn.resumecraft.mapper.ResumeMapper;
import cn.resumecraft.model.Resume;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResumeService {
  private final ResumeMapper resumeMapper;
  private final TemplateService templateService;
  private final ResumeContentValidator contentValidator;

  public ResumeService(
      ResumeMapper resumeMapper,
      TemplateService templateService,
      ResumeContentValidator contentValidator) {
    this.resumeMapper = resumeMapper;
    this.templateService = templateService;
    this.contentValidator = contentValidator;
  }

  public List<Resume> list(Long userId) {
    return resumeMapper.selectList(
        new LambdaQueryWrapper<Resume>()
            .eq(Resume::getUserId, userId)
            .orderByDesc(Resume::getUpdatedAt));
  }

  public Resume owned(Long userId, Long resumeId) {
    Resume resume = resumeMapper.selectById(resumeId);
    require(resume != null, 404, "简历不存在");
    require(resume.userId.equals(userId), 403, "无权访问这份简历");
    return resume;
  }

  @Transactional
  public Resume create(Long userId, Long templateId) {
    templateService.checkAccess(userId, templateId);
    Resume resume = newDraft(userId, templateId);
    resume.title = "我的简历-" + templateService.get(templateId).name;
    try {
      resume.content =
          new ClassPathResource("sample-resume.json").getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to read sample resume", exception);
    }
    resumeMapper.insert(resume);
    templateService.recordUse(templateId);
    return resume;
  }

  @Transactional
  public Resume save(Long userId, Long resumeId, SaveResumeRequest request) {
    owned(userId, resumeId);
    templateService.checkAccess(userId, request.templateId());
    contentValidator.validate(request.content());
    int updated =
        resumeMapper.update(
            null,
            new LambdaUpdateWrapper<Resume>()
                .eq(Resume::getId, resumeId)
                .eq(Resume::getUserId, userId)
                .eq(Resume::getVersion, request.version())
                .set(Resume::getTitle, request.title())
                .set(Resume::getTemplateId, request.templateId())
                .set(Resume::getContent, request.content().toString())
                .set(Resume::getVersion, request.version() + 1)
                .set(Resume::getUpdatedAt, LocalDateTime.now()));
    require(updated == 1, 409, "简历已在其他窗口更新，请刷新后重试；本地草稿已保留");
    return resumeMapper.selectById(resumeId);
  }

  @Transactional
  public Resume duplicate(Long userId, Long resumeId) {
    Resume source = owned(userId, resumeId);
    templateService.checkAccess(userId, source.templateId);
    Resume duplicate = newDraft(userId, source.templateId);
    duplicate.title = source.title.substring(0, Math.min(90, source.title.length())) + "-副本";
    duplicate.content = source.content;
    resumeMapper.insert(duplicate);
    return duplicate;
  }

  public void delete(Long userId, Long resumeId) {
    owned(userId, resumeId);
    resumeMapper.deleteById(resumeId);
  }

  private Resume newDraft(Long userId, Long templateId) {
    Resume resume = new Resume();
    resume.userId = userId;
    resume.templateId = templateId;
    resume.version = 0;
    resume.status = "DRAFT";
    resume.createdAt = LocalDateTime.now();
    resume.updatedAt = resume.createdAt;
    return resume;
  }
}
