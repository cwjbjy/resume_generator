package cn.resumecraft;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cn.resumecraft.exception.BizException;
import cn.resumecraft.service.ResumeContentValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;

class ResumeContentValidatorTest {
  private final ObjectMapper json = new ObjectMapper();
  private final ResumeContentValidator validator = new ResumeContentValidator();

  @Test
  void rootAndEveryModuleMustHaveTheExpectedShape() throws Exception {
    assertInvalid(null);
    assertInvalid(json.readTree("[]"));
    assertInvalid(json.readTree("{}"));
    for (String module : new String[] {"basic", "edu", "work", "proj", "skills", "custom"}) {
      ObjectNode content = sample();
      content.remove(module);
      assertInvalid(content);
      content.putNull(module);
      assertInvalid(content);
      content.put(module, "invalid");
      assertInvalid(content);
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"name", "job", "phone", "email", "city", "intro"})
  void basicFieldsMustBePresentText(String field) throws Exception {
    ObjectNode content = sample();
    ObjectNode basic = (ObjectNode) content.path("basic");
    basic.remove(field);
    assertInvalid(content);
    basic.putNull(field);
    assertInvalid(content);
    basic.put(field, 123);
    assertInvalid(content);
    basic.put(field, "");
    assertThatCode(() -> validator.validate(content)).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "null",
        "1",
        "true",
        "[]",
        "\"text\"",
        "{}",
        "{\"id\":null}",
        "{\"id\":1}",
        "{\"id\":\"x\",\"company\":null}",
        "{\"id\":\"x\",\"desc\":12}",
        "{\"id\":\"x\",\"start\":\"2024-13\"}",
        "{\"id\":\"x\",\"start\":\"2024-1\"}",
        "{\"id\":\"x\",\"start\":\"2024-02\",\"end\":\"2024-01\"}"
      })
  void experienceEntriesMustMatchTheirDeclaredTypes(String entry) throws Exception {
    for (String module : new String[] {"edu", "work", "proj", "custom"}) {
      ObjectNode content = sample();
      content.putArray(module).add(json.readTree(entry));
      assertInvalid(content);
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"null", "1", "true", "[]", "{}"})
  void skillsMustBeText(String skill) throws Exception {
    ObjectNode content = sample();
    content.putArray("skills").add(json.readTree(skill));
    assertInvalid(content);
  }

  @Test
  void sampleAndIncompleteEditableEntriesRemainSupported() throws Exception {
    ObjectNode content = sample();
    assertThatCode(() -> validator.validate(content)).doesNotThrowAnyException();
    content.putArray("custom").addObject().put("id", "new").put("title", "").put("desc", "");
    assertThatCode(() -> validator.validate(content)).doesNotThrowAnyException();
    content.putArray("edu").addObject().put("id", "new").put("start", "").put("end", "");
    assertThatCode(() -> validator.validate(content)).doesNotThrowAnyException();
  }

  @Test
  void duplicateIdsAndOversizedModulesAreRejected() throws Exception {
    ObjectNode content = sample();
    content.withArray("edu").add(content.path("edu").get(0).deepCopy());
    assertInvalid(content);
    content = sample();
    for (int index = 0; index < 101; index++)
      content.withArray("custom").addObject().put("id", "entry" + index);
    assertInvalid(content);
    content = sample();
    ((ObjectNode) content.path("basic")).put("intro", "字".repeat(200001));
    assertInvalid(content);
  }

  private ObjectNode sample() throws IOException {
    return (ObjectNode)
        json.readTree(
            new ClassPathResource("sample-resume.json").getContentAsString(StandardCharsets.UTF_8));
  }

  private void assertInvalid(JsonNode content) {
    assertThatThrownBy(() -> validator.validate(content))
        .isInstanceOfSatisfying(
            BizException.class, exception -> assertThat(exception.status).isEqualTo(400));
  }
}
