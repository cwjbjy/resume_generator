package cn.resumecraft.dto.ai;

import java.util.List;

public record AiPolishResponse(
    String original,
    String suggested,
    List<String> notes,
    Integer version,
    String module,
    String entryId) {}
