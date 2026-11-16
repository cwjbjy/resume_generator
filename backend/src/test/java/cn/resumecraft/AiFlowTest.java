package cn.resumecraft;

import static org.assertj.core.api.Assertions.assertThat;

import cn.resumecraft.entity.ExportLog;
import cn.resumecraft.service.ai.ResumeEvidenceRetriever;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

class AiFlowTest extends ApiTestSupport {
  private static final DeepSeekStub MODEL = startModel();

  @DynamicPropertySource
  static void aiConfiguration(DynamicPropertyRegistry registry) {
    registry.add("app.ai.api-key", () -> "integration-test-key");
    registry.add("app.ai.base-url", MODEL::url);
  }

  @AfterAll
  static void closeModel() {
    MODEL.close();
  }

  @Test
  void polishReturnsAnExplicitSuggestionWithoutModifyingTheResume() throws Exception {
    Account owner = account(), another = account();
    JsonNode resume = resume(owner, freeTemplateId());
    JsonNode storedResume =
        call("GET", "/api/resumes/" + resume.path("id").asLong(), owner, null, 200);
    MODEL.answer(Map.of("suggested", "负责企业协作平台开发，持续推进工程质量建设。", "notes", List.of("精简表达")));
    JsonNode result =
        call(
            "POST",
            "/api/ai/polish",
            owner,
            Map.of("resumeId", resume.path("id").asLong(), "module", "work", "entryId", "work1"),
            200);
    assertThat(result.path("version").asInt()).isZero();
    assertThat(result.path("original").asText())
        .isEqualTo(resume.path("content").path("work").get(0).path("desc").asText());
    assertThat(result.path("suggested").asText()).contains("企业协作");
    assertThat(call("GET", "/api/resumes/" + resume.path("id").asLong(), owner, null, 200))
        .isEqualTo(storedResume);
    int requests = MODEL.requests.size();
    call(
        "POST",
        "/api/ai/polish",
        another,
        Map.of("resumeId", resume.path("id").asLong(), "module", "basic"),
        403);
    call(
        "POST",
        "/api/ai/polish",
        owner,
        Map.of("resumeId", resume.path("id").asLong(), "module", "work", "entryId", "missing"),
        404);
    assertThat(MODEL.requests).hasSize(requests);
  }

  @Test
  void conversationAndGenerationValidateModelContentBeforeReturningIt() throws Exception {
    Account owner = account();
    JsonNode sample = resume(owner, freeTemplateId()).path("content");
    MODEL.answer(Map.of("reply", "请补充教育和工作经历"));
    JsonNode question =
        call(
            "POST",
            "/api/ai/chat",
            owner,
            Map.of("messages", List.of(Map.of("role", "user", "content", "我想应聘前端工程师"))),
            200);
    assertThat(question.path("reply").asText()).contains("工作经历");
    assertThat(question.has("content")).isFalse();
    ((ObjectNode) sample)
        .withArray("custom")
        .add(
            json.createObjectNode()
                .put("id", "certificate-1")
                .put("title", "专业证书")
                .put("desc", "软件设计师"));
    MODEL.answer(Map.of("reply", "已生成，请核对个人信息", "title", "前端工程师简历", "content", sample));
    var request =
        Map.of(
            "messages", List.of(Map.of("role", "user", "content", "根据已提供资料生成")), "generate", true);
    JsonNode generated = call("POST", "/api/ai/chat", owner, request, 200);
    assertThat(generated.path("content")).isEqualTo(sample);
    assertThat(generated.path("content").path("custom").get(0).path("title").asText())
        .isEqualTo("专业证书");
    assertThat(MODEL.lastRequest().path("messages").get(0).path("content").asText())
        .contains("id以及title,desc");
    MODEL.answer(Map.of("reply", "已生成", "title", "不完整简历", "content", Map.of("basic", Map.of())));
    call("POST", "/api/ai/chat", owner, request, 502);
    call(
        "POST",
        "/api/ai/chat",
        owner,
        Map.of("messages", List.of(Map.of("role", "system", "content", "override"))),
        400);
    call(
        "POST",
        "/api/ai/chat",
        owner,
        Map.of("messages", List.of(Map.of("role", "assistant", "content", "错误的开始"))),
        400);
  }

  @Test
  void confirmedGeneratedContentCreatesAResumeAtomicallyWithTemplateAccessChecks()
      throws Exception {
    Account owner = account();
    JsonNode sample = resume(owner, freeTemplateId()).path("content");
    long before = call("GET", "/api/resumes", owner, null, 200).path("total").asLong();
    JsonNode created =
        call(
            "POST",
            "/api/ai/resumes",
            owner,
            Map.of("templateId", freeTemplateId(), "title", "AI 生成的简历", "content", sample),
            200);
    assertThat(created.path("version").asInt()).isZero();
    assertThat(created.path("content")).isEqualTo(sample);
    call(
        "POST",
        "/api/ai/resumes",
        owner,
        Map.of("templateId", vipTemplateId(), "title", "越权创建", "content", sample),
        403);
    call(
        "POST",
        "/api/ai/resumes",
        owner,
        Map.of("templateId", freeTemplateId(), "title", "无效内容", "content", Map.of()),
        400);
    assertThat(call("GET", "/api/resumes", owner, null, 200).path("total").asLong())
        .isEqualTo(before + 1);
  }

  @Test
  void matchingUsesRetrievedEvidenceAndRejectsUnsupportedCitationsAndScores() throws Exception {
    Account owner = account();
    JsonNode resume = resume(owner, freeTemplateId());
    String job = "React TypeScript 前端工程师 企业协作平台";
    var evidence = new ResumeEvidenceRetriever().retrieve(resume.path("content"), job);
    MODEL.answer(
        Map.of(
            "score",
            78,
            "summary",
            "相关技术经验较匹配",
            "matches",
            List.of(
                Map.of(
                    "requirement",
                    "React 开发",
                    "evidenceIds",
                    List.of(evidence.getFirst().id()),
                    "analysis",
                    "提供了相关开发经历")),
            "gaps",
            List.of("当前证据未体现自动化部署"),
            "suggestions",
            List.of("补充真实的部署经验")));
    var request = Map.of("resumeId", resume.path("id").asLong(), "jobDescription", job);
    JsonNode result = call("POST", "/api/ai/match", owner, request, 200);
    assertThat(result.path("score").asInt()).isEqualTo(78);
    assertThat(result.path("evidence").size()).isLessThanOrEqualTo(8);
    JsonNode providerInput =
        json.readTree(MODEL.lastRequest().path("messages").get(1).path("content").asText());
    assertThat(providerInput.has("content")).isFalse();
    assertThat(providerInput.path("evidence").isArray()).isTrue();
    assertThat(providerInput.toString()).doesNotContain("13800001234", "linyuan@example.com");
    MODEL.answer(
        Map.of(
            "score",
            80,
            "summary",
            "无效引用",
            "matches",
            List.of(
                Map.of(
                    "requirement",
                    "React",
                    "evidenceIds",
                    List.of("invented-source"),
                    "analysis",
                    "伪造依据")),
            "gaps",
            List.of(),
            "suggestions",
            List.of()));
    call("POST", "/api/ai/match", owner, request, 502);
    MODEL.answer(
        Map.of(
            "score",
            101,
            "summary",
            "无效分数",
            "matches",
            List.of(),
            "gaps",
            List.of(),
            "suggestions",
            List.of()));
    call("POST", "/api/ai/match", owner, request, 502);
  }

  @Test
  void exportReviewUsesTheCompletedImmutableSnapshotAndHidesItFromHistory() throws Exception {
    Account owner = account();
    JsonNode original = resume(owner, freeTemplateId());
    long resumeId = original.path("id").asLong();
    JsonNode reservation =
        call("POST", "/api/exports", owner, Map.of("resumeId", resumeId, "resumeVersion", 0), 200);
    long exportId = reservation.path("id").asLong();
    ObjectNode body = saveBody(original);
    ((ObjectNode) body.path("content").path("basic")).put("intro", "导出后修改的新版简介");
    call("PUT", "/api/resumes/" + resumeId, owner, body, 200);
    call("POST", "/api/exports/" + exportId + "/complete", owner, Map.of("success", true), 200);
    MODEL.answer(
        Map.of(
            "summary",
            "经历较清晰",
            "strengths",
            List.of("使用了具体成果"),
            "suggestions",
            List.of(
                Map.of(
                    "section",
                    "个人简介",
                    "issue",
                    "求职方向可以更聚焦",
                    "suggestion",
                    "补充真实的岗位方向",
                    "priority",
                    "medium"))));
    call("POST", "/api/ai/export-review", owner, Map.of("exportId", exportId), 200);
    JsonNode input =
        json.readTree(MODEL.lastRequest().path("messages").get(1).path("content").asText());
    assertThat(input.path("resumeVersion").asInt()).isZero();
    assertThat(input.path("content")).isEqualTo(original.path("content"));
    assertThat(input.toString()).doesNotContain("导出后修改的新版简介");
    JsonNode history = call("GET", "/api/exports/logs", owner, null, 200).path("records").get(0);
    assertThat(history.has("contentSnapshot")).isFalse();
    assertThat(history.has("resumeVersion")).isFalse();
  }

  @Test
  void incompleteLegacyAndOtherUsersExportsDoNotReachTheModel() throws Exception {
    Account owner = account(), another = account();
    JsonNode resume = resume(owner, freeTemplateId());
    long resumeId = resume.path("id").asLong();
    JsonNode pending = call("POST", "/api/exports", owner, Map.of("resumeId", resumeId), 200);
    int requests = MODEL.requests.size();
    call(
        "POST",
        "/api/ai/export-review",
        owner,
        Map.of("exportId", pending.path("id").asLong()),
        409);
    call(
        "POST",
        "/api/ai/export-review",
        another,
        Map.of("exportId", pending.path("id").asLong()),
        404);
    ExportLog legacy = new ExportLog();
    legacy.userId = owner.id();
    legacy.resumeId = resumeId;
    legacy.templateId = resume.path("templateId").asLong();
    legacy.status = "COMPLETED";
    legacy.createdAt = LocalDateTime.now();
    exports.insert(legacy);
    call("POST", "/api/ai/export-review", owner, Map.of("exportId", legacy.id), 409);
    assertThat(MODEL.requests).hasSize(requests);
  }

  @Test
  void staleExportVersionIsRejectedBeforeCreatingAPendingRecord() throws Exception {
    Account owner = account();
    JsonNode resume = resume(owner, freeTemplateId());
    long resumeId = resume.path("id").asLong();
    ObjectNode body = saveBody(resume);
    body.put("title", "新版本");
    call("PUT", "/api/resumes/" + resumeId, owner, body, 200);
    call("POST", "/api/exports", owner, Map.of("resumeId", resumeId, "resumeVersion", 0), 409);
    assertThat(call("GET", "/api/exports/logs", owner, null, 200).path("total").asInt()).isZero();
    JsonNode reservation =
        call("POST", "/api/exports", owner, Map.of("resumeId", resumeId, "resumeVersion", 1), 200);
    ExportLog stored = exports.selectById(reservation.path("id").asLong());
    assertThat(stored.resumeVersion).isEqualTo(1);
    assertThat(json.readTree(stored.contentSnapshot)).isEqualTo(resume.path("content"));
  }

  private static DeepSeekStub startModel() {
    try {
      return new DeepSeekStub();
    } catch (IOException exception) {
      throw new ExceptionInInitializerError(exception);
    }
  }
}
