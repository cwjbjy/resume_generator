package cn.resumecraft.dto.ai;

import java.util.List;

public record AiReviewResponse(
    String summary, List<String> strengths, List<Suggestion> suggestions) {
  public record Suggestion(String section, String issue, String suggestion, String priority) {}
}
