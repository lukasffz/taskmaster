package com.taskmaster.services;

import com.taskmaster.dtos.task.CreateTaskRequest;
import com.taskmaster.dtos.task.TaskResponse;
import com.taskmaster.dtos.task.UpdateTaskRequest;
import com.taskmaster.exceptions.ResourceNotFoundException;
import com.taskmaster.exceptions.UnauthorizedAccessException;
import com.taskmaster.models.Project;
import com.taskmaster.models.Task;
import com.taskmaster.models.TaskStatus;
import com.taskmaster.models.User;
import com.taskmaster.repositories.ProjectRepository;
import com.taskmaster.repositories.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserService userService;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserService userService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    @Transactional
    public TaskResponse create(Long projectId, CreateTaskRequest request) {
        User owner = userService.getAuthenticatedUser();
        Project project = resolveOwnedProject(projectId, owner.getId());

        Task task = new Task(
                request.getTitle(),
                request.getDescription(),
                request.getStatus(),
                project
        );

        Task saved = taskRepository.save(task);
        log.info("Task created: id={}, project={}, owner={}", saved.getId(), projectId, owner.getId());
        return TaskResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listByProject(Long projectId, TaskStatus status) {
        User owner = userService.getAuthenticatedUser();
        resolveOwnedProject(projectId, owner.getId());

        List<Task> tasks = (status != null)
                ? taskRepository.findByProjectIdAndStatusOrderByCreatedAtDesc(projectId, status)
                : taskRepository.findByProjectIdOrderByCreatedAtDesc(projectId);

        return tasks.stream().map(TaskResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(Long projectId, Long taskId) {
        User owner = userService.getAuthenticatedUser();
        resolveOwnedProject(projectId, owner.getId());
        Task task = resolveTaskInProject(taskId, projectId);
        return TaskResponse.fromEntity(task);
    }

    @Transactional
    public TaskResponse update(Long projectId, Long taskId, UpdateTaskRequest request) {
        User owner = userService.getAuthenticatedUser();
        resolveOwnedProject(projectId, owner.getId());

        Task task = resolveTaskInProject(taskId, projectId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());

        Task saved = taskRepository.save(task);
        log.info("Task updated: id={}, project={}, owner={}", saved.getId(), projectId, owner.getId());
        return TaskResponse.fromEntity(saved);
    }

    @Transactional
    public void delete(Long projectId, Long taskId) {
        User owner = userService.getAuthenticatedUser();
        resolveOwnedProject(projectId, owner.getId());

        Task task = resolveTaskInProject(taskId, projectId);
        taskRepository.delete(task);
        log.info("Task deleted: id={}, project={}, owner={}", taskId, projectId, owner.getId());
    }

    /**
     * Verifies that the authenticated user owns the project before any task operation.
     * This is the primary ownership guard — tasks inherit their access control from projects.
     */
    private Project resolveOwnedProject(Long projectId, Long ownerId) {
        return projectRepository.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> {
                    if (projectRepository.existsById(projectId)) {
                        throw new UnauthorizedAccessException(
                                "Access denied to project with id: " + projectId);
                    }
                    return new ResourceNotFoundException(
                            "Project not found with id: " + projectId);
                });
    }

    /**
     * Resolves a task by id within a specific project.
     * Because we already verified project ownership above, a missing task here is always a 404.
     */
    private Task resolveTaskInProject(Long taskId, Long projectId) {
        return taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found with id: " + taskId + " in project: " + projectId));
    }
}
