package com.riloidx.todolist.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskPositionDto(
        @NotNull(message = "Position can't be null")
        @Min(value = 1, message = "Position cannot be negative")
        Integer position
) {
}
