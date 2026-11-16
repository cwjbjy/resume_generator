package cn.resumecraft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cn.resumecraft.dto.ai.AiMatchResponse.Evidence;
import cn.resumecraft.exception.BizException;
import cn.resumecraft.service.ResumeContentValidator;
import cn.resumecraft.service.ai.AiOutputValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AiOutputValidatorTest {
  private final ObjectMapper json = new ObjectMapper();
  private final AiOutputValidator validator = new AiOutputValidator(new ResumeContentValidator());

  @Test
  void modelSchemaErrorsAlwaysUseBadGatewayStatus() {
    assertBadModel(() -> validator.text(json.valueToTree(Map.of("text", 1)), "text", 100));
    assertBadModel(
        () -> validator.texts(json.valueToTree(Map.of("notes", List.of(1))), "notes", 8, 100));
    assertBadModel(
        () ->
            validator.chat(
                json.valueToTree(Map.of("reply", "生成完成", "title", "标题", "content", Map.of())),
                true));
    assertBadModel(
        () ->
            validator.review(
                json.valueToTree(
                    Map.of(
                        "summary",
                        "建议",
                        "strengths",
                        List.of(),
                        "suggestions",
                        List.of(
                            Map.of(
                                "section",
                                "简介",
                                "issue",
                                "较长",
                                "suggestion",
                                "精简",
                                "priority",
                                "urgent"))))));
  }

  @Test
  void matchScoresAndCitationsMustHaveRetrievedSupport() {
    var evidence = List.of(new Evidence("work-1", "work", "React开发", 1));
    for (Object score : new Object[] {-1, 101, 75.5, "75"}) {
      assertBadModel(
          () ->
              validator.match(
                  json.valueToTree(
                      Map.of(
                          "score",
                          score,
                          "summary",
                          "匹配",
                          "matches",
                          List.of(),
                          "gaps",
                          List.of(),
                          "suggestions",
                          List.of())),
                  evidence));
    }
    assertBadModel(
        () ->
            validator.match(
                json.valueToTree(
                    Map.of(
                        "score",
                        70,
                        "summary",
                        "匹配",
                        "matches",
                        List.of(),
                        "gaps",
                        List.of(),
                        "suggestions",
                        List.of())),
                List.of()));
    var empty =
        validator.match(
            json.valueToTree(
                Map.of(
                    "score",
                    0,
                    "summary",
                    "当前证据不足",
                    "matches",
                    List.of(),
                    "gaps",
                    List.of("补充真实经历"),
                    "suggestions",
                    List.of())),
            List.of());
    assertThat(empty.score()).isZero();
    assertThat(empty.evidence()).isEmpty();
  }

  private void assertBadModel(Runnable call) {
    assertThatThrownBy(call::run)
        .isInstanceOfSatisfying(
            BizException.class, error -> assertThat(error.status).isEqualTo(502));
  }
}
