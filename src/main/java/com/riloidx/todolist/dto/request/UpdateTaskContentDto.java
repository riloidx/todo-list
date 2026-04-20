package com.riloidx.todolist.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateTaskContentDto(
        @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
        String title,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description
) {
}
