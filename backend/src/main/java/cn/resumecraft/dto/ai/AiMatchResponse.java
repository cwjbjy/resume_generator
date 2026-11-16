package cn.resumecraft.dto.ai;

import java.util.List;

public record AiMatchResponse(
    int score,
    String summary,
    List<Match> matches,
    List<String> gaps,
    List<String> suggestions,
    List<Evidence> evidence) {
  public record Match(String requirement, List<String> evidenceIds, String analysis) {}

  public record Evidence(String id, String section, String text, double score) {}
}
