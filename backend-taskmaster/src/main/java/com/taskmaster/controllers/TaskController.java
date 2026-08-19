package com.taskmaster.controllers;

import com.taskmaster.dtos.task.CreateTaskRequest;
import com.taskmaster.dtos.task.TaskResponse;
import com.taskmaster.dtos.task.UpdateTaskRequest;
import com.taskmaster.models.TaskStatus;
import com.taskmaster.services.TaskService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@Tag(name = "Tasks", description = "Task management operations")
@SecurityRequirement(name = "cookieAuth")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
        @Operation(summary = "Create a task in a project")
        @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Project belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Project not found")
        })
    public ResponseEntity<TaskResponse> create(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request) {
        TaskResponse response = taskService.create(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
        @Operation(summary = "List tasks in a project")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tasks returned"),
            @ApiResponse(responseCode = "403", description = "Project belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Project not found")
        })
    public ResponseEntity<List<TaskResponse>> listByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) TaskStatus status) {
        return ResponseEntity.ok(taskService.listByProject(projectId, status));
    }

    @GetMapping("/{taskId}")
        @Operation(summary = "Get a task by ID")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task returned"),
            @ApiResponse(responseCode = "403", description = "Resource belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Task not found")
        })
    public ResponseEntity<TaskResponse> findById(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.findById(projectId, taskId));
    }

    @PutMapping("/{taskId}")
        @Operation(summary = "Update a task")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Resource belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Task not found")
        })
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.update(projectId, taskId, request));
    }

    @DeleteMapping("/{taskId}")
        @Operation(summary = "Delete a task")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task deleted"),
            @ApiResponse(responseCode = "403", description = "Resource belongs to another user"),
            @ApiResponse(responseCode = "404", description = "Task not found")
        })
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        taskService.delete(projectId, taskId);
        return ResponseEntity.ok(Map.of("message", "Task deleted successfully"));
    }
}
