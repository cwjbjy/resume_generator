package cn.resumecraft.service;

import static cn.resumecraft.exception.BizException.require;

import cn.resumecraft.dto.ResumeResponse;
import cn.resumecraft.dto.ResumeSummaryResponse;
import cn.resumecraft.dto.SaveResumeRequest;
import cn.resumecraft.entity.Resume;
import cn.resumecraft.mapper.ResumeMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResumeService {
  private final ResumeMapper resumeMapper;
  private final TemplateService templateService;
  private final ResumeContentValidator contentValidator;
  private final ObjectMapper objectMapper;

  public IPage<ResumeSummaryResponse> list(Long userId, int page, int size) {
    return resumeMapper
        .selectSummaries(new Page<>(page, size), userId)
        .convert(resume -> ResumeSummaryResponse.from(resume, readContent(resume.content)));
  }

  public ResumeResponse response(Resume resume) {
    return ResumeResponse.from(resume, readContent(resume.content));
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
  public Resume createFromContent(Long userId, Long templateId, String title, JsonNode content) {
    templateService.checkAccess(userId, templateId);
    contentValidator.validate(content);
    Resume resume = newDraft(userId, templateId);
    resume.title = title;
    resume.content = content.toString();
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

  private JsonNode readContent(String content) {
    try {
      return objectMapper.readTree(content);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Unable to read stored resume content", exception);
    }
  }
}
