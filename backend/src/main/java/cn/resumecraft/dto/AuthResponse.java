package cn.resumecraft.dto;

import cn.resumecraft.model.User;

public record AuthResponse(String token, User user) {}
