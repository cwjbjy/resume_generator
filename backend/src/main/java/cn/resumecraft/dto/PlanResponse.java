package cn.resumecraft.dto;

import cn.resumecraft.entity.Plan;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public record PlanResponse(
    Long id, String name, Integer price, Integer durationDays, List<String> features) {
  public static PlanResponse from(Plan plan, ObjectMapper objectMapper) {
    try {
      List<String> features = objectMapper.readValue(plan.features, new TypeReference<>() {});
      return new PlanResponse(plan.id, plan.name, plan.price, plan.durationDays, features);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("套餐权益配置无效", exception);
    }
  }
}
