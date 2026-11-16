package cn.resumecraft.service.ai;

import static cn.resumecraft.exception.BizException.require;

import cn.resumecraft.dto.ai.AiChatResponse;
import cn.resumecraft.dto.ai.AiMatchResponse;
import cn.resumecraft.dto.ai.AiReviewResponse;
import cn.resumecraft.exception.BizException;
import cn.resumecraft.service.ResumeContentValidator;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiOutputValidator {
  private final ResumeContentValidator contentValidator;

  public String text(JsonNode object, String field, int maxLength) {
    JsonNode value = object.path(field);
    require(
        value.isTextual()
            && !value.textValue().isBlank()
            && value.textValue().length() <= maxLength,
        502,
        "AI 返回的文本字段无效，请重试");
    return value.textValue();
  }

  public List<String> texts(JsonNode object, String field, int limit, int maxLength) {
    JsonNode values = array(object, field, limit);
    List<String> result = new ArrayList<>();
    for (JsonNode value : values) {
      require(
          value.isTextual()
              && !value.textValue().isBlank()
              && value.textValue().length() <= maxLength,
          502,
          "AI 返回的列表内容无效，请重试");
      result.add(value.textValue());
    }
    return List.copyOf(result);
  }

  public AiChatResponse chat(JsonNode result, boolean generate) {
    String reply = text(result, "reply", 4000);
    if (!generate) return new AiChatResponse(reply, null, null);
    String title = text(result, "title", 100);
    JsonNode content = result.path("content");
    try {
      contentValidator.validate(content);
    } catch (BizException exception) {
      throw new BizException(502, "AI 生成的简历结构无效，请补充信息后重新生成");
    }
    return new AiChatResponse(reply, title, content);
  }

  public AiMatchResponse match(JsonNode result, List<AiMatchResponse.Evidence> evidence) {
    JsonNode score = result.path("score");
    require(
        score.isIntegralNumber()
            && score.canConvertToInt()
            && score.intValue() >= 0
            && score.intValue() <= 100,
        502,
        "AI 返回的匹配分值无效，请重试");
    require(!evidence.isEmpty() || score.intValue() == 0, 502, "AI 评分缺少简历证据，请重新分析");
    Set<String> knownIds = new HashSet<>();
    evidence.forEach(item -> knownIds.add(item.id()));
    List<AiMatchResponse.Match> matches = new ArrayList<>();
    for (JsonNode item : array(result, "matches", 20)) {
      List<String> ids = texts(item, "evidenceIds", 8, 100);
      require(!ids.isEmpty() && knownIds.containsAll(ids), 502, "AI 引用了无效的简历证据，请重新分析");
      matches.add(
          new AiMatchResponse.Match(
              text(item, "requirement", 1000), ids, text(item, "analysis", 2000)));
    }
    require(score.intValue() == 0 || !matches.isEmpty(), 502, "AI 返回的匹配结论缺少引用证据，请重新分析");
    return new AiMatchResponse(
        score.intValue(),
        text(result, "summary", 3000),
        List.copyOf(matches),
        texts(result, "gaps", 20, 1500),
        texts(result, "suggestions", 20, 1500),
        evidence);
  }

  public AiReviewResponse review(JsonNode result) {
    List<AiReviewResponse.Suggestion> suggestions = new ArrayList<>();
    for (JsonNode item : array(result, "suggestions", 20)) {
      String priority = text(item, "priority", 10);
      require(Set.of("high", "medium", "low").contains(priority), 502, "AI 返回的建议优先级无效，请重试");
      suggestions.add(
          new AiReviewResponse.Suggestion(
              text(item, "section", 100),
              text(item, "issue", 2000),
              text(item, "suggestion", 3000),
              priority));
    }
    return new AiReviewResponse(
        text(result, "summary", 3000),
        texts(result, "strengths", 10, 1500),
        List.copyOf(suggestions));
  }

  private JsonNode array(JsonNode object, String field, int limit) {
    JsonNode value = object.path(field);
    require(value.isArray() && value.size() <= limit, 502, "AI 返回的列表格式无效，请重试");
    return value;
  }
}
