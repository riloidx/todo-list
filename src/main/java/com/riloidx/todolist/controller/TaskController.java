package com.riloidx.todolist.controller;

import com.riloidx.todolist.dto.request.CreateTaskDto;
import com.riloidx.todolist.dto.request.UpdateTaskContentDto;
import com.riloidx.todolist.dto.request.UpdateTaskCompletedDto;
import com.riloidx.todolist.dto.request.UpdateTaskPositionDto;
import com.riloidx.todolist.dto.response.TaskResponseDto;
import com.riloidx.todolist.service.TaskService;
import com.riloidx.todolist.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<TaskResponseDto> create(@Valid @RequestBody CreateTaskDto createTaskDto) {
        var task = taskService.create(createTaskDto, securityUtils.getCurrentUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> findById(@PathVariable long id) {
        var task = taskService.findById(id, securityUtils.getCurrentUserId());

        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @GetMapping("/active")
    public ResponseEntity<List<TaskResponseDto>> findAllActive() {
        var tasks = taskService.findAllActiveTasks(securityUtils.getCurrentUserId());

        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<TaskResponseDto>> findAllCompleted(@RequestParam(defaultValue = "true") boolean status) {
        var tasks = taskService.findAllByUserIdAndCompleted(securityUtils.getCurrentUserId(), status);

        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateContent(@PathVariable long id,
                                                  @Valid @RequestBody UpdateTaskContentDto updateTaskContentDto) {
        var task = taskService.update(id, updateTaskContentDto, securityUtils.getCurrentUserId());

        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @PatchMapping("/{id}/completed")
    public ResponseEntity<TaskResponseDto> updateCompleted(@PathVariable long id,
                                                  @Valid @RequestBody UpdateTaskCompletedDto updateTaskCompletedDto) {
        var task = taskService.updateCompleted(id, updateTaskCompletedDto, securityUtils.getCurrentUserId());

        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @PatchMapping("/{id}/position")
    public ResponseEntity<TaskResponseDto> updatePosition(@PathVariable long id,
                                                  @Valid @RequestBody UpdateTaskPositionDto updateTaskPositionDto) {
        var task = taskService.updatePosition(id, updateTaskPositionDto, securityUtils.getCurrentUserId());

        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        taskService.delete(id, securityUtils.getCurrentUserId());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}