package com.riloidx.todolist.service;

import com.riloidx.todolist.dto.request.CreateTaskDto;
import com.riloidx.todolist.dto.request.UpdateTaskDto;
import com.riloidx.todolist.dto.response.TaskResponseDto;
import com.riloidx.todolist.exception.TaskNotFoundException;
import com.riloidx.todolist.mapper.TaskMapper;
import com.riloidx.todolist.model.Task;
import com.riloidx.todolist.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepo;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private final String userId = "user-123";
    private final long taskId = 1L;
    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task();
        task.setId(taskId);
        task.setUserId(userId);
        task.setTitle("Test Task");
        task.setPosition(5);
        task.setCompleted(false);
    }

    @Test
    void createShouldSetPositionOneAndIncrementOthers() {
        CreateTaskDto dto = new CreateTaskDto("Title", "Desc");
        when(taskMapper.toEntity(dto)).thenReturn(task);
        when(taskRepo.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(mock(TaskResponseDto.class));

        taskService.create(dto, userId);

        assertThat(task.getPosition()).isEqualTo(1);
        verify(taskRepo).incrementPositionsFrom(1, userId);
        verify(taskRepo).save(task);
    }

    @Test
    void findByIdShouldReturnDtoWhenValidOwner() {
        when(taskRepo.findById(taskId)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(new TaskResponseDto(taskId, "Title", null, false, 5, null, null));

        TaskResponseDto result = taskService.findById(taskId, userId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(taskId);
    }

    @Test
    void findByIdShouldThrowNotFoundWhenTaskMissing() {
        when(taskRepo.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(taskId, userId))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void findAllByUserIdAndCompletedShouldReturnList() {
        boolean completed = true;
        when(taskRepo.findAllByUserIdAndCompletedOrderByUpdatedAtDesc(userId, completed))
                .thenReturn(List.of(task));
        when(taskMapper.toDto(task)).thenReturn(mock(TaskResponseDto.class));

        List<TaskResponseDto> results = taskService.findAllByUserIdAndCompleted(userId, completed);

        assertThat(results).hasSize(1);
        verify(taskRepo).findAllByUserIdAndCompletedOrderByUpdatedAtDesc(userId, completed);
    }

    @Test
    void findAllActiveTasksShouldReturnListOrderedByPositionAsc() {
        when(taskRepo.findAllByUserIdAndCompletedFalseOrderByPositionAsc(userId))
                .thenReturn(List.of(task));
        when(taskMapper.toDto(task)).thenReturn(mock(TaskResponseDto.class));

        List<TaskResponseDto> results = taskService.findAllActiveTasks(userId);

        assertThat(results).hasSize(1);
        verify(taskRepo).findAllByUserIdAndCompletedFalseOrderByPositionAsc(userId);
    }

    @Test
    void updateShouldThrowIllegalStateExceptionWhenEditingCompletedTask() {
        task.setCompleted(true);
        UpdateTaskDto updateDto = new UpdateTaskDto("New", "New", true, 0);
        when(taskRepo.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.update(taskId, updateDto, userId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void updateShouldHandleStatusChangeToCompleted() {
        int oldPos = task.getPosition();
        UpdateTaskDto updateDto = new UpdateTaskDto("Title", "Desc", true, 0);

        when(taskRepo.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepo.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(mock(TaskResponseDto.class));

        taskService.update(taskId, updateDto, userId);

        assertThat(task.getCompleted()).isTrue();
        assertThat(task.getPosition()).isEqualTo(0);
        verify(taskRepo).decrementPositionsFrom(oldPos, userId);
        verify(taskRepo).save(task);
    }

    @Test
    void updateShouldHandleStatusChangeToActive() {
        task.setCompleted(true);
        task.setPosition(0);
        UpdateTaskDto updateDto = new UpdateTaskDto("Title", "Desc", false, 1);

        when(taskRepo.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepo.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(mock(TaskResponseDto.class));

        taskService.update(taskId, updateDto, userId);

        assertThat(task.getCompleted()).isFalse();
        assertThat(task.getPosition()).isEqualTo(1);
        verify(taskRepo).incrementPositionsFrom(1, userId);
        verify(taskRepo).save(task);
    }

    @Test
    void deleteShouldCallRepoDeleteAndDecrementPositions() {
        int pos = task.getPosition();
        when(taskRepo.findById(taskId)).thenReturn(Optional.of(task));

        taskService.delete(taskId, userId);

        verify(taskRepo).deleteById(taskId);
        verify(taskRepo).decrementPositionsFrom(pos, userId);
    }

    @Test
    void updateShouldThrowAccessDeniedWhenNotOwner() {
        task.setUserId("attacker");
        UpdateTaskDto updateDto = new UpdateTaskDto("Title", "Desc", false, 1);
        when(taskRepo.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.update(taskId, updateDto, userId))
                .isInstanceOf(AccessDeniedException.class);
        verify(taskRepo, never()).save(any());
    }
}