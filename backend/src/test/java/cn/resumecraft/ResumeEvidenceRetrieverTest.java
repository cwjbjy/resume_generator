package cn.resumecraft;

import static org.assertj.core.api.Assertions.assertThat;

import cn.resumecraft.service.ai.ResumeEvidenceRetriever;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class ResumeEvidenceRetrieverTest {
  private final ObjectMapper json = new ObjectMapper();
  private final ResumeEvidenceRetriever retriever = new ResumeEvidenceRetriever();

  @Test
  void retrievesRelevantChineseAndEnglishEvidenceWithoutContactInformation() throws Exception {
    var evidence = retriever.retrieve(sample(), "前端工程师 React TypeScript 企业协作平台");
    assertThat(evidence).isNotEmpty().hasSizeLessThanOrEqualTo(8);
    assertThat(evidence.getFirst().score()).isPositive();
    String text = json.writeValueAsString(evidence);
    assertThat(text).contains("React").doesNotContain("13800001234", "linyuan@example.com", "林予安");
    for (int index = 1; index < evidence.size(); index++) {
      assertThat(evidence.get(index).score()).isLessThanOrEqualTo(evidence.get(index - 1).score());
    }
  }

  @Test
  void doesNotInventEvidenceWhenTheQueryHasNoOverlap() throws Exception {
    assertThat(retriever.retrieve(sample(), "Kubernetes Rust aerospace")).isEmpty();
    assertThat(retriever.retrieve(json.readTree("{}"), "React")).isEmpty();
  }

  @Test
  void limitsLongDocumentsToEightBoundedEvidenceChunks() throws Exception {
    ObjectNode content = sample();
    content.withArray("work").removeAll();
    for (int index = 0; index < 30; index++) {
      content
          .withArray("work")
          .addObject()
          .put("id", "work-" + index)
          .put("company", "React Team")
          .put("desc", "React 工程化实践 ".repeat(200));
    }
    var evidence = retriever.retrieve(content, "React 工程化");
    assertThat(evidence).hasSize(8);
    assertThat(evidence)
        .allSatisfy(chunk -> assertThat(chunk.text().length()).isLessThanOrEqualTo(900));
  }

  private ObjectNode sample() throws Exception {
    return (ObjectNode)
        json.readTree(
            new ClassPathResource("sample-resume.json").getContentAsString(StandardCharsets.UTF_8));
  }
}
