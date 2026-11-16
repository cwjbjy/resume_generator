package cn.resumecraft.service;

import static cn.resumecraft.exception.BizException.require;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ResumeContentValidator {
  public void validate(JsonNode content) {
    require(
        content != null && content.isObject() && content.toString().length() <= 200000,
        400,
        "简历内容格式错误或超过大小限制");
    JsonNode basic = content.path("basic");
    require(basic.isObject(), 400, "请填写基本信息");
    for (String field : new String[] {"name", "job", "phone", "email", "city", "intro"}) {
      require(basic.path(field).isTextual(), 400, "基本信息字段必须为文本且不能缺失");
    }
    String phone = basic.path("phone").textValue();
    require(phone.isBlank() || phone.matches("[+0-9()\\- ]{6,25}"), 400, "电话号码格式不正确");
    for (String module : new String[] {"edu", "work", "proj", "skills", "custom"}) {
      JsonNode entries = content.path(module);
      require(entries.isArray() && entries.size() <= 100, 400, "模块格式错误或条目过多");
      Set<String> entryIds = new HashSet<>();
      for (JsonNode entry : entries) {
        if (module.equals("skills")) {
          require(entry.isTextual(), 400, "技能必须为文本");
        } else {
          validateEntry(entry);
          require(entryIds.add(entry.path("id").textValue()), 400, "同一模块的条目标识不能重复");
        }
      }
    }
  }

  private void validateEntry(JsonNode entry) {
    require(entry.isObject(), 400, "经历条目必须为对象");
    require(
        entry.path("id").isTextual() && !entry.path("id").textValue().isBlank(), 400, "经历条目标识不能为空");
    for (String field :
        new String[] {
          "school", "major", "degree", "company", "role", "name", "title", "start", "end", "desc"
        }) {
      require(!entry.has(field) || entry.path(field).isTextual(), 400, "经历字段必须为文本");
    }
    validateDates(entry);
  }

  private void validateDates(JsonNode entry) {
    String start = entry.path("start").asText("");
    String end = entry.path("end").asText("");
    for (String date : new String[] {start, end}) {
      require(date.isEmpty() || date.matches("[0-9]{4}-(0[1-9]|1[0-2])"), 400, "日期格式应为 YYYY-MM");
    }
    require(start.isBlank() || end.isBlank() || start.compareTo(end) <= 0, 400, "结束时间不能早于开始时间");
  }
}
