package cn.resumecraft.dto;

import cn.resumecraft.entity.Template;

public record TemplateResponse(
    Long id,
    String name,
    String category,
    Boolean isVip,
    String color,
    String style,
    Integer usedCount) {
  public static TemplateResponse from(Template template) {
    return new TemplateResponse(
        template.id,
        template.name,
        template.category,
        template.isVip,
        template.color,
        template.style,
        template.usedCount);
  }
}
