package com.riloidx.todolist.service;

import com.riloidx.todolist.dto.request.CreateTaskDto;
import com.riloidx.todolist.dto.request.UpdateTaskDto;
import com.riloidx.todolist.dto.response.TaskResponseDto;
import com.riloidx.todolist.exception.TaskNotFoundException;
import com.riloidx.todolist.mapper.TaskMapper;
import com.riloidx.todolist.model.Task;
import com.riloidx.todolist.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepo;
    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public TaskResponseDto create(CreateTaskDto createTaskDto, String userId) {
        log.info("Creating new task for user: {}", userId);

        Task task = taskMapper.toEntity(createTaskDto);
        task.setUserId(userId);

        Integer lastPosition = taskRepo.findMaxPositionByUserId(task.getUserId());
        int newPosition = (lastPosition == null ? 0 : lastPosition + 1);
        task.setPosition(newPosition);

        log.debug("Task position set to: {}", newPosition);

        Task savedTask = taskRepo.save(task);
        log.info("Task created successfully with id: {}", savedTask.getId());

        return taskMapper.toDto(savedTask);
    }

    @Override
    public TaskResponseDto findById(long id, String userId) {
        log.debug("Finding task {} for user {}", id, userId);
        return taskMapper.toDto(findEntityByIdAndCheckOwner(id, userId));
    }

    @Override
    public List<TaskResponseDto> findAllByUserIdAndCompleted(String userId, boolean completed) {
        log.debug("Fetching tasks for user {} with completed status: {}", userId, completed);
        return taskRepo.findAllByUserIdAndCompletedOrderByUpdatedAtDesc(userId, completed)
                .stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @Override
    public List<TaskResponseDto> findAllActiveTasks(String userId) {
        log.debug("Fetching all active tasks for user: {}", userId);
        return taskRepo.findAllByUserIdAndCompletedFalseOrderByPositionDesc(userId)
                .stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public TaskResponseDto update(long id, UpdateTaskDto updateTaskDto, String userId) {
        log.info("Updating task {} for user {}", id, userId);

        Task curTask = findEntityByIdAndCheckOwner(id, userId);
        taskMapper.updateEntityFromDto(updateTaskDto, curTask);

        Task savedTask = taskRepo.save(curTask);
        log.info("Task {} updated successfully", id);

        return taskMapper.toDto(savedTask);
    }

    @Override
    @Transactional
    public void delete(long id, String userId) {
        log.info("Deleting task {} for user {}", id, userId);

        findEntityByIdAndCheckOwner(id, userId);
        taskRepo.deleteById(id);

        log.info("Task {} deleted successfully", id);
    }

    private Task findEntityByIdAndCheckOwner(long id, String userId) {
        Task task = taskRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn("Task not found with id: {}", id);
                    return new TaskNotFoundException("id", String.valueOf(id));
                });

        if (!task.getUserId().equals(userId)) {
            log.error("Security violation: User {} tried to access task {} owned by {}",
                    userId, id, task.getUserId());
            throw new AccessDeniedException("Access denied to task: " + id);
        }
        return task;
    }
}