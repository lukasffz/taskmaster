package com.taskmaster.services;

import com.taskmaster.dtos.task.CreateTaskRequest;
import com.taskmaster.dtos.task.TaskResponse;
import com.taskmaster.dtos.task.UpdateTaskRequest;
import com.taskmaster.exceptions.ResourceNotFoundException;
import com.taskmaster.exceptions.UnauthorizedAccessException;
import com.taskmaster.models.Project;
import com.taskmaster.models.Role;
import com.taskmaster.models.Task;
import com.taskmaster.models.TaskStatus;
import com.taskmaster.models.User;
import com.taskmaster.repositories.ProjectRepository;
import com.taskmaster.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskService taskService;

    private User owner;
    private Project project;
    private Task task;

    @BeforeEach
    void setUp() {
        owner = new User("Lukas User", "lukas@test.com", "encodedPassword", Role.USER);
        owner.setId(1L);

        project = new Project("My Project", "A description", owner);
        project.setId(10L);

        task = new Task("My Task", "Task description", TaskStatus.TODO, project);
        task.setId(100L);
    }

    @Test
    void shouldCreateTaskSuccessfully() {
        CreateTaskRequest request = new CreateTaskRequest("My Task", "Task description", null);
        when(userService.getAuthenticatedUser()).thenReturn(owner);
        when(projectRepository.findByIdAndOwnerId(project.getId(), owner.getId()))
                .thenReturn(Optional.of(project));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.create(project.getId(), request);

        assertNotNull(response);
        assertEquals("My Task", response.getTitle());
        assertEquals(project.getId(), response.getProjectId());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void shouldThrowNotFoundWhenCreatingTaskInNonExistentProject() {
        CreateTaskRequest request = new CreateTaskRequest("Task", null, null);
        when(userService.getAuthenticatedUser()).thenReturn(owner);
        when(projectRepository.findByIdAndOwnerId(99L, owner.getId())).thenReturn(Optional.empty());
        when(projectRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> taskService.create(99L, request));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void shouldThrowUnauthorizedWhenCreatingTaskInAnotherUsersProject() {
        CreateTaskRequest request = new CreateTaskRequest("Task", null, null);
        when(userService.getAuthenticatedUser()).thenReturn(owner);
        when(projectRepository.findByIdAndOwnerId(project.getId(), owner.getId())).thenReturn(Optional.empty());
        when(projectRepository.existsById(project.getId())).thenReturn(true);

        assertThrows(UnauthorizedAccessException.class, () -> taskService.create(project.getId(), request));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void shouldListTasksWithoutStatusFilter() {
        when(userService.getAuthenticatedUser()).thenReturn(owner);
        when(projectRepository.findByIdAndOwnerId(project.getId(), owner.getId()))
                .thenReturn(Optional.of(project));
        when(taskRepository.findByProjectIdOrderByCreatedAtDesc(project.getId()))
                .thenReturn(List.of(task));

        List<TaskResponse> responses = taskService.listByProject(project.getId(), null);

        assertEquals(1, responses.size());
        assertEquals("My Task", responses.get(0).getTitle());
    }

    @Test
    void shouldListTasksFilteredByStatus() {
        when(userService.getAuthenticatedUser()).thenReturn(owner);
        when(projectRepository.findByIdAndOwnerId(project.getId(), owner.getId()))
                .thenReturn(Optional.of(project));
        when(taskRepository.findByProjectIdAndStatusOrderByCreatedAtDesc(project.getId(), TaskStatus.TODO))
                .thenReturn(List.of(task));

        List<TaskResponse> responses = taskService.listByProject(project.getId(), TaskStatus.TODO);

        assertEquals(1, responses.size());
        assertEquals(TaskStatus.TODO, responses.get(0).getStatus());
    }

    @Test
    void shouldReturnTaskByIdSuccessfully() {
        when(userService.getAuthenticatedUser()).thenReturn(owner);
        when(projectRepository.findByIdAndOwnerId(project.getId(), owner.getId()))
                .thenReturn(Optional.of(project));
        when(taskRepository.findByIdAndProjectId(task.getId(), project.getId()))
                .thenReturn(Optional.of(task));

        TaskResponse response = taskService.findById(project.getId(), task.getId());

        assertEquals(task.getId(), response.getId());
    }

    @Test
    void shouldThrowNotFoundWhenTaskDoesNotExistInProject() {
        when(userService.getAuthenticatedUser()).thenReturn(owner);
        when(projectRepository.findByIdAndOwnerId(project.getId(), owner.getId()))
                .thenReturn(Optional.of(project));
        when(taskRepository.findByIdAndProjectId(999L, project.getId()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taskService.findById(project.getId(), 999L));
    }

    @Test
    void shouldUpdateTaskSuccessfully() {
        UpdateTaskRequest request = new UpdateTaskRequest("Updated title", "Updated desc", TaskStatus.IN_PROGRESS);
        when(userService.getAuthenticatedUser()).thenReturn(owner);
        when(projectRepository.findByIdAndOwnerId(project.getId(), owner.getId()))
                .thenReturn(Optional.of(project));
        when(taskRepository.findByIdAndProjectId(task.getId(), project.getId()))
                .thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.update(project.getId(), task.getId(), request);

        assertNotNull(response);
        verify(taskRepository).save(task);
    }

    @Test
    void shouldDeleteTaskSuccessfully() {
        when(userService.getAuthenticatedUser()).thenReturn(owner);
        when(projectRepository.findByIdAndOwnerId(project.getId(), owner.getId()))
                .thenReturn(Optional.of(project));
        when(taskRepository.findByIdAndProjectId(task.getId(), project.getId()))
                .thenReturn(Optional.of(task));

        assertDoesNotThrow(() -> taskService.delete(project.getId(), task.getId()));
        verify(taskRepository).delete(task);
    }

    @Test
    void shouldNotDeleteTaskFromAnotherUsersProject() {
        when(userService.getAuthenticatedUser()).thenReturn(owner);
        when(projectRepository.findByIdAndOwnerId(project.getId(), owner.getId())).thenReturn(Optional.empty());
        when(projectRepository.existsById(project.getId())).thenReturn(true);

        assertThrows(UnauthorizedAccessException.class,
                () -> taskService.delete(project.getId(), task.getId()));
        verify(taskRepository, never()).delete(any());
    }
}
