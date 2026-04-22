package com.riloidx.todolist.service;

import com.riloidx.todolist.dto.request.CreateTaskDto;
import com.riloidx.todolist.dto.request.UpdateTaskContentDto;
import com.riloidx.todolist.dto.request.UpdateTaskPositionDto;
import com.riloidx.todolist.dto.request.UpdateTaskCompletedDto;
import com.riloidx.todolist.dto.response.TaskResponseDto;
import com.riloidx.todolist.exception.TaskAlreadyInStateException;
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
    public TaskResponseDto updateContent(long id, UpdateTaskContentDto updateTaskDto, String userId) {
        log.info("Updating task {} for user {}", id, userId);

        Task curTask = findEntityByIdAndCheckOwner(id, userId);

        taskMapper.updateEntityFromDto(updateTaskDto, curTask);

        Task savedTask = taskRepo.save(curTask);
        log.info("Task {} updated successfully", id);

        return taskMapper.toDto(savedTask);
    }

    @Override
    @Transactional
    public TaskResponseDto updateCompleted(long id, UpdateTaskCompletedDto updateTaskCompletedDto, String userId) {
        Task curTask = findEntityByIdAndCheckOwner(id, userId);

        if (updateTaskCompletedDto.completed() == curTask.getCompleted()) {
            throw new TaskAlreadyInStateException("Task already have completed=" + curTask.getCompleted());
        }

        handleStatusChange(curTask, updateTaskCompletedDto.completed(), userId);

        return taskMapper.toDto(curTask);
    }

    @Override
    @Transactional
    public TaskResponseDto updatePosition(long id, UpdateTaskPositionDto updateTaskPositionDto, String userId) {
        Task curTask = findEntityByIdAndCheckOwner(id, userId);

        if (updateTaskPositionDto.position().equals(curTask.getPosition())) {
            throw new TaskAlreadyInStateException("Task already have position=" + curTask.getPosition());
        }

        reorderTasks(curTask.getPosition(), updateTaskPositionDto.position(), userId);
        curTask.setPosition(updateTaskPositionDto.position());

        return taskMapper.toDto(taskRepo.save(curTask));
    }

    @Override
    @Transactional
    public void delete(long id, String userId) {
        log.info("Deleting task {} for user {}", id, userId);

        Task curTask = findEntityByIdAndCheckOwner(id, userId);
        taskRepo.decrementPositionsFrom(curTask.getPosition(), userId);
        taskRepo.deleteById(id);

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
            taskRepo.decrementPositionsFrom(oldPos, userId);
            task.setCompleted(true);
            task.setPosition(0);
        } else {
            taskRepo.incrementPositionsFrom(1, userId);
            task.setCompleted(false);
            task.setPosition(1);
        }
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