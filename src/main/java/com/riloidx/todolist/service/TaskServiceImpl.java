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
import org.springframework.boot.data.metrics.DefaultRepositoryTagsProvider;
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

        task.setPosition(1);
        taskRepo.incrementPositionsFrom(task.getPosition(), userId);

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
        return taskRepo.findAllByUserIdAndCompletedFalseOrderByPositionAsc(userId)
                .stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public TaskResponseDto update(long id, UpdateTaskDto updateTaskDto, String userId) {
        log.info("Updating task {} for user {}", id, userId);

        Task curTask = findEntityByIdAndCheckOwner(id, userId);

        Boolean completed = updateTaskDto.completed();
        Integer newPos = updateTaskDto.position();

        if (newPos != null && !newPos.equals(curTask.getPosition())) {
            reorderTasks(curTask.getPosition(), newPos, userId);
            curTask.setPosition(newPos);
        }

        if (completed != null) {
            if (Boolean.TRUE.equals(curTask.getCompleted()) && completed) {
                log.warn("User {} tried to edit a completed task {}", userId, id);
                throw new IllegalStateException("Cannot edit a completed task. Restore it first.");
            }

            if (!completed.equals(curTask.getCompleted())) {
                handleStatusChange(curTask, completed, userId);
            }
        }

        taskMapper.updateEntityFromDto(updateTaskDto, curTask);

        Task savedTask = taskRepo.save(curTask);
        log.info("Task {} updated successfully", id);

        return taskMapper.toDto(savedTask);
    }

    @Override
    @Transactional
    public void delete(long id, String userId) {
        log.info("Deleting task {} for user {}", id, userId);

        Task curTask = findEntityByIdAndCheckOwner(id, userId);
        taskRepo.deleteById(id);
        taskRepo.flush();

        taskRepo.decrementPositionsFrom(curTask.getPosition(), userId);

        log.info("Task {} deleted successfully", id);
    }

    private void reorderTasks(int oldPos, int newPos, String userId) {
        if (oldPos < newPos) {
            taskRepo.decrementPositionsInRange(oldPos + 1, newPos, userId);
        } else {
            taskRepo.incrementPositionsInRange(newPos, oldPos, userId);
        }
    }

    private void handleStatusChange(Task task, boolean isNowCompleted, String userId) {
        if (isNowCompleted) {
            int oldPos = task.getPosition();
            task.setCompleted(true);
            task.setPosition(0);
            taskRepo.decrementPositionsFrom(oldPos, userId);
        } else {
            task.setCompleted(false);
            task.setPosition(1);
            taskRepo.incrementPositionsFrom(1, userId);
        }
        taskRepo.flush();
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