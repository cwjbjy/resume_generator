package cn.resumecraft.service.ai;

import cn.resumecraft.dto.ai.AiMatchResponse.Evidence;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ResumeEvidenceRetriever {
  private static final Pattern ENGLISH = Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9+#.]*");
  private static final Pattern CHINESE = Pattern.compile("[\\p{IsHan}]+");
  private static final int CHUNK_LENGTH = 900;

  public List<Evidence> retrieve(JsonNode content, String jobDescription) {
    List<Evidence> chunks = chunks(content);
    if (chunks.isEmpty()) return List.of();
    List<Map<String, Integer>> frequencies =
        chunks.stream().map(chunk -> tokenize(chunk.text())).toList();
    Map<String, Integer> documentFrequency = new HashMap<>();
    for (Map<String, Integer> frequency : frequencies) {
      for (String term : frequency.keySet()) documentFrequency.merge(term, 1, Integer::sum);
    }
    double averageLength =
        frequencies.stream()
            .mapToInt(frequency -> frequency.values().stream().mapToInt(Integer::intValue).sum())
            .average()
            .orElse(1);
    Set<String> query = tokenize(jobDescription).keySet();
    List<Evidence> ranked = new ArrayList<>();
    for (int index = 0; index < chunks.size(); index++) {
      Map<String, Integer> frequency = frequencies.get(index);
      int length = frequency.values().stream().mapToInt(Integer::intValue).sum();
      double score = 0;
      for (String term : query) {
        int count = frequency.getOrDefault(term, 0);
        if (count == 0) continue;
        double idf =
            Math.log1p(
                (chunks.size() - documentFrequency.get(term) + 0.5)
                    / (documentFrequency.get(term) + 0.5));
        score += idf * count * 2.2 / (count + 1.2 * (0.25 + 0.75 * length / averageLength));
      }
      if (score > 0) {
        Evidence chunk = chunks.get(index);
        ranked.add(
            new Evidence(
                chunk.id(), chunk.section(), chunk.text(), Math.round(score * 1000.0) / 1000.0));
      }
    }
    return ranked.stream()
        .sorted(Comparator.comparingDouble(Evidence::score).reversed())
        .limit(8)
        .toList();
  }

  private List<Evidence> chunks(JsonNode content) {
    List<Evidence> chunks = new ArrayList<>();
    addChunks(
        chunks,
        "basic",
        "个人简介",
        content.path("basic").path("job").asText(""),
        content.path("basic").path("intro").asText(""));
    List<String> skills = new ArrayList<>();
    content.path("skills").forEach(skill -> skills.add(skill.asText()));
    addChunks(chunks, "skills", "专业技能", "", String.join("、", skills));
    for (String group : new String[] {"edu", "work", "proj", "custom"}) {
      int index = 0;
      for (JsonNode entry : content.path(group)) {
        List<String> heading = new ArrayList<>();
        for (String field :
            new String[] {
              "school", "major", "degree", "company", "role", "name", "title", "start", "end"
            }) {
          if (entry.hasNonNull(field) && !entry.path(field).asText().isBlank()) {
            heading.add(entry.path(field).asText());
          }
        }
        addChunks(
            chunks,
            group + "-" + (++index),
            group,
            String.join(" / ", heading),
            entry.path("desc").asText(""));
      }
    }
    return chunks;
  }

  private void addChunks(
      List<Evidence> chunks, String id, String section, String heading, String text) {
    String combined = (heading + "\n" + text).trim();
    for (int offset = 0; offset < combined.length(); offset += CHUNK_LENGTH) {
      String part = combined.substring(offset, Math.min(combined.length(), offset + CHUNK_LENGTH));
      chunks.add(new Evidence(id + "-" + (offset / CHUNK_LENGTH + 1), section, part, 0));
    }
  }

  private Map<String, Integer> tokenize(String text) {
    Map<String, Integer> terms = new HashMap<>();
    var english = ENGLISH.matcher(text.toLowerCase(Locale.ROOT));
    while (english.find()) terms.merge(english.group(), 1, Integer::sum);
    var chinese = CHINESE.matcher(text);
    while (chinese.find()) {
      String segment = chinese.group();
      if (segment.length() == 1) terms.merge(segment, 1, Integer::sum);
      for (int index = 0; index < segment.length() - 1; index++) {
        terms.merge(segment.substring(index, index + 2), 1, Integer::sum);
      }
    }
    return terms;
  }
}
