package cn.resumecraft.dto.ai;

import com.fasterxml.jackson.databind.JsonNode;

public record AiChatResponse(String reply, String title, JsonNode content) {}
