package com.riloidx.todolist.service;

import com.riloidx.todolist.dto.request.CreateTaskDto;
import com.riloidx.todolist.dto.request.UpdateTaskDto;
import com.riloidx.todolist.dto.response.TaskResponseDto;

import java.util.List;

public interface TaskService {
    TaskResponseDto create(CreateTaskDto createTaskDto, String userId);

    TaskResponseDto findById(long id, String userId);

    List<TaskResponseDto> findAllByUserIdAndCompleted(String userId, boolean completed);

    List<TaskResponseDto> findAllActiveTasks(String userId);

    TaskResponseDto update(long id, UpdateTaskDto updateTaskDto, String userId);

    void delete(long id, String userId);
}
