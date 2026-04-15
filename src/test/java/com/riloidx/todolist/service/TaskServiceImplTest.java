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
        task.setPosition(0);
    }

    @Test
    void createShouldSetPositionZeroWhenNoTasksExist() {
        CreateTaskDto dto = new CreateTaskDto("Title", "Desc");
        when(taskMapper.toEntity(dto)).thenReturn(task);
        when(taskRepo.findMaxPositionByUserId(userId)).thenReturn(null);
        when(taskRepo.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(new TaskResponseDto(taskId, "Title", "Desc", false, 0, null, null));

        TaskResponseDto result = taskService.create(dto, userId);

        assertThat(task.getPosition()).isEqualTo(0);
        assertThat(result).isNotNull();
        verify(taskRepo).save(task);
    }

    @Test
    void createShouldIncrementPositionWhenTasksExist() {
        CreateTaskDto dto = new CreateTaskDto("Title", "Desc");
        int maxPos = 10;
        when(taskMapper.toEntity(dto)).thenReturn(task);
        when(taskRepo.findMaxPositionByUserId(userId)).thenReturn(maxPos);
        when(taskRepo.save(any(Task.class))).thenReturn(task);

        taskService.create(dto, userId);

        assertThat(task.getPosition()).isEqualTo(maxPos + 1);
        verify(taskRepo).save(task);
    }

    @Test
    void findByIdShouldReturnDtoWhenValidOwner() {
        when(taskRepo.findById(taskId)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(new TaskResponseDto(taskId, "Title", null, false, 0, null, null));

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
    void findByIdShouldThrowAccessDeniedWhenWrongUser() {
        task.setUserId("other-user");
        when(taskRepo.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.findById(taskId, userId))
                .isInstanceOf(AccessDeniedException.class);
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
    void findAllActiveTasksShouldReturnList() {
        when(taskRepo.findAllByUserIdAndCompletedFalseOrderByPositionDesc(userId))
                .thenReturn(List.of(task));
        when(taskMapper.toDto(task)).thenReturn(mock(TaskResponseDto.class));

        List<TaskResponseDto> results = taskService.findAllActiveTasks(userId);

        assertThat(results).hasSize(1);
        verify(taskRepo).findAllByUserIdAndCompletedFalseOrderByPositionDesc(userId);
    }

    @Test
    void updateShouldUpdateAndSave() {
        UpdateTaskDto updateDto = new UpdateTaskDto("New Title", "New Desc", true, 5);
        when(taskRepo.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepo.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(mock(TaskResponseDto.class));

        taskService.update(taskId, updateDto, userId);

        verify(taskMapper).updateEntityFromDto(updateDto, task);
        verify(taskRepo).save(task);
    }

    @Test
    void updateShouldThrowAccessDeniedWhenNotOwner() {
        task.setUserId("attacker");
        UpdateTaskDto updateDto = new UpdateTaskDto("Title", "Desc", true, 1);
        when(taskRepo.findById(taskId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.update(taskId, updateDto, userId))
                .isInstanceOf(AccessDeniedException.class);
        verify(taskRepo, never()).save(any());
    }

    @Test
    void deleteShouldCallRepoDelete() {
        when(taskRepo.findById(taskId)).thenReturn(Optional.of(task));

        taskService.delete(taskId, userId);

        verify(taskRepo).deleteById(taskId);
    }

    @Test
    void deleteShouldThrowNotFoundWhenTaskDoesNotExist() {
        when(taskRepo.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(taskId, userId))
                .isInstanceOf(TaskNotFoundException.class);
        verify(taskRepo, never()).deleteById(anyLong());
    }
}