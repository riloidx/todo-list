package com.riloidx.todolist.dto.response;

import java.time.Instant;

public record TaskResponseDto(
        Long id,
        String title,
        String description,
        Boolean completed,
        Integer position,
        Instant createdAt,
        Instant updatedAt
) {
}
