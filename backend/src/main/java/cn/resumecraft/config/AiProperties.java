package cn.resumecraft.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "app.ai")
@Validated
@Getter
@Setter
public class AiProperties {
  private String apiKey = "";
  @NotBlank private String baseUrl = "https://api.deepseek.com";
  @NotBlank private String model = "deepseek-flash";

  @Min(1)
  @Max(180)
  private int timeoutSeconds = 60;
}
