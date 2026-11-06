package cn.resumecraft.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CredentialsRequest(
    @NotBlank(message = "请输入用户名")
        @Pattern(regexp = "[a-zA-Z0-9_]{4,32}", message = "用户名为 4–32 位字母、数字或下划线")
        String username,
    @Size(min = 6, max = 72, message = "密码长度为 6–72 位") @NotNull(message = "请输入密码") String password,
    @Email(message = "邮箱格式不正确") @Size(max = 120) String email) {}
