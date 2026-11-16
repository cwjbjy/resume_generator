package cn.resumecraft.dto.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AiChatRequest(
    @NotEmpty @Size(max = 20) List<@NotNull @Valid Message> messages, boolean generate) {
  public record Message(
      @NotBlank @Pattern(regexp = "user|assistant") String role,
      @NotBlank @Size(max = 4000) String content) {}
}
