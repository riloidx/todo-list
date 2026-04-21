package com.riloidx.todolist.service;

import com.riloidx.todolist.dto.request.*;
import com.riloidx.todolist.dto.response.TaskResponseDto;
import com.riloidx.todolist.exception.TaskAlreadyInStateException;
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
        task.setDescription("Description");
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
    void updateContentShouldOnlyUpdateContentText() {
        UpdateTaskContentDto updateDto = new UpdateTaskContentDto("New Title", "New Desc");
        when(taskRepo.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepo.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(mock(TaskResponseDto.class));

        taskService.updateContent(taskId, updateDto, userId);

        verify(taskMapper).updateEntityFromDto(updateDto, task);
        verify(taskRepo).save(task);

        assertThat(task.getPosition()).isEqualTo(5);
        assertThat(task.getCompleted()).isFalse();
    }

    @Test
    void updateContentCompletedShouldHandleChangeToTrue() {
        UpdateTaskCompletedDto dto = new UpdateTaskCompletedDto(true);
        when(taskRepo.findById(taskId)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(mock(TaskResponseDto.class));

        taskService.updateCompleted(taskId, dto, userId);

        assertThat(task.getCompleted()).isTrue();
        assertThat(task.getPosition()).isEqualTo(0);
        verify(taskRepo).decrementPositionsFrom(5, userId);
        verify(taskRepo).flush();
    }

    @Test
    void updateContentCompletedShouldThrowIfStateIsSame() {
        UpdateTaskCompletedDto dto = new UpdateTaskCompletedDto(false);
        when(taskRepo.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.updateCompleted(taskId, dto, userId))
                .isInstanceOf(TaskAlreadyInStateException.class);
    }

    @Test
    void updateContentPositionShouldHandleMoveDown() {
        int newPos = 8;
        UpdateTaskPositionDto dto = new UpdateTaskPositionDto(newPos);
        when(taskRepo.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepo.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(mock(TaskResponseDto.class));

        taskService.updatePosition(taskId, dto, userId);

        assertThat(task.getPosition()).isEqualTo(newPos);
        verify(taskRepo).decrementPositionsInRange(6, 8, userId);
        verify(taskRepo).save(task);
    }

    @Test
    void updateContentPositionShouldHandleMoveUp() {
        int newPos = 2;
        UpdateTaskPositionDto dto = new UpdateTaskPositionDto(newPos);
        when(taskRepo.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepo.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(mock(TaskResponseDto.class));

        taskService.updatePosition(taskId, dto, userId);

        assertThat(task.getPosition()).isEqualTo(newPos);
        verify(taskRepo).incrementPositionsInRange(2, 5, userId);
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
    void findByIdShouldThrowAccessDeniedWhenNotOwner() {
        task.setUserId("other-user");
        when(taskRepo.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.findById(taskId, userId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void findByIdShouldThrowNotFoundWhenTaskMissing() {
        when(taskRepo.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(taskId, userId))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void findAllActiveTasksShouldReturnList() {
        when(taskRepo.findAllByUserIdAndCompletedFalseOrderByPositionAsc(userId))
                .thenReturn(List.of(task));
        when(taskMapper.toDto(task)).thenReturn(mock(TaskResponseDto.class));

        List<TaskResponseDto> results = taskService.findAllActiveTasks(userId);

        assertThat(results).hasSize(1);
        verify(taskRepo).findAllByUserIdAndCompletedFalseOrderByPositionAsc(userId);
    }
}