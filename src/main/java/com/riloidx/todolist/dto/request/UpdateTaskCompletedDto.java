package com.riloidx.todolist.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateTaskCompletedDto(
        @NotNull(message = "Status can't be null")
        Boolean completed
) {
}
