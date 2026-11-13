package cn.resumecraft.dto;

import cn.resumecraft.entity.User;

public record AuthResponse(String token, User user) {}
