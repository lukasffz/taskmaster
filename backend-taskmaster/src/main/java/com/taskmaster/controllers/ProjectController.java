package com.taskmaster.controllers;

import com.taskmaster.dtos.project.CreateProjectRequest;
import com.taskmaster.dtos.project.ProjectResponse;
import com.taskmaster.dtos.project.UpdateProjectRequest;
import com.taskmaster.services.ProjectService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Project management operations")
@SecurityRequirement(name = "cookieAuth")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
        @Operation(summary = "Create a project")
        @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Project created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
        })
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List the current user's projects")
    @ApiResponse(responseCode = "200", description = "Projects returned")
    public ResponseEntity<List<ProjectResponse>> listAll() {
        return ResponseEntity.ok(projectService.listAllForCurrentUser());
    }

    @GetMapping("/{id}")
        @Operation(summary = "Get a project by ID")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project returned"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "Project belongs to another user")
        })
    public ResponseEntity<ProjectResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.findById(id));
    }

    @PutMapping("/{id}")
        @Operation(summary = "Update a project")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "Project belongs to another user")
        })
    public ResponseEntity<ProjectResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(projectService.update(id, request));
    }

    @DeleteMapping("/{id}")
        @Operation(summary = "Delete a project")
        @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project deleted"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "Project belongs to another user")
        })
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Project deleted successfully"));
    }
}
