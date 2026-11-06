package cn.resumecraft.service;

import static cn.resumecraft.api.BizException.require;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class ResumeContentValidator {
  public void validate(JsonNode content) {
    require(
        content != null && content.isObject() && content.toString().length() <= 200000,
        400,
        "简历内容格式错误或超过大小限制");
    require(content.path("basic").isObject(), 400, "请填写基本信息");
    String phone = content.path("basic").path("phone").asText("");
    require(phone.isBlank() || phone.matches("[+0-9()\\- ]{6,25}"), 400, "电话号码格式不正确");
    for (String module : new String[] {"edu", "work", "proj", "skills", "custom"}) {
      JsonNode entries = content.path(module);
      require(entries.isArray() && entries.size() <= 100, 400, "模块格式错误或条目过多");
      if (!module.equals("skills")) {
        for (JsonNode entry : entries) {
          validateDates(entry);
        }
      }
    }
  }

  private void validateDates(JsonNode entry) {
    String start = entry.path("start").asText("");
    String end = entry.path("end").asText("");
    require(start.isBlank() || end.isBlank() || start.compareTo(end) <= 0, 400, "结束时间不能早于开始时间");
  }
}
