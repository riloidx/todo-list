package com.riloidx.todolist.service;

import com.riloidx.todolist.dto.request.CreateTaskDto;
import com.riloidx.todolist.dto.request.UpdateTaskContentDto;
import com.riloidx.todolist.dto.request.UpdateTaskPositionDto;
import com.riloidx.todolist.dto.request.UpdateTaskCompletedDto;
import com.riloidx.todolist.dto.response.TaskResponseDto;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface TaskService {
    TaskResponseDto create(CreateTaskDto createTaskDto, String userId);

    TaskResponseDto findById(long id, String userId);

    List<TaskResponseDto> findAllByUserIdAndCompleted(String userId, boolean completed);

    List<TaskResponseDto> findAllActiveTasks(String userId);

    TaskResponseDto update(long id, UpdateTaskContentDto updateTaskDto, String userId);

    TaskResponseDto updateCompleted(long id, UpdateTaskCompletedDto updateTaskCompletedDto, String userId);

    TaskResponseDto updatePosition(long id, UpdateTaskPositionDto updateTaskPositionDto, String userId);

    void delete(long id, String userId);
}
