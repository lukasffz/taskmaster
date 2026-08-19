package com.taskmaster.services;

import com.taskmaster.dtos.project.CreateProjectRequest;
import com.taskmaster.dtos.project.ProjectResponse;
import com.taskmaster.dtos.project.UpdateProjectRequest;
import com.taskmaster.exceptions.ResourceNotFoundException;
import com.taskmaster.exceptions.UnauthorizedAccessException;
import com.taskmaster.models.Project;
import com.taskmaster.models.Role;
import com.taskmaster.models.User;
import com.taskmaster.repositories.ProjectRepository;
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
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProjectService projectService;

    private User owner;
    private Project project;

    @BeforeEach
    void setUp() {
        owner = new User("Lukas User", "lukas@test.com", "encodedPassword", Role.USER);
        owner.setId(1L);

        project = new Project("My Project", "A description", owner);
        project.setId(10L);
    }

    @Test
    void shouldCreateProjectSuccessfully() {
        CreateProjectRequest request = new CreateProjectRequest("My Project", "A description");
        when(userService.getAuthenticatedUser()).thenReturn(owner);
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponse response = projectService.create(request);

        assertNotNull(response);
        assertEquals("My Project", response.getName());
        assertEquals(owner.getId(), response.getOwnerId());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void shouldListAllProjectsForCurrentUser() {
        when(userService.getAuthenticatedUser()).thenReturn(owner);
        when(projectRepository.findByOwnerIdOrderByCreatedAtDesc(owner.getId()))
                .thenReturn(List.of(project));

        List<ProjectResponse> responses = projectService.listAllForCurrentUser();

        assertEquals(1, responses.size());
        assertEquals("My Project", responses.get(0).getName());
    }

    @Test
    void shouldReturnProjectByIdWhenOwnerMatches() {
        when(userService.getAuthenticatedUser()).thenReturn(owner);
        when(projectRepository.findByIdAndOwnerId(project.getId(), owner.getId()))
                .thenReturn(Optional.of(project));

        ProjectResponse response = projectService.findById(project.getId());

        assertEquals(project.getId(), response.getId());
        assertEquals("My Project", response.getName());
    }

    @Test
    void shouldThrowNotFoundWhenProjectDoesNotExist() {
        when(userService.getAuthenticatedUser()).thenReturn(owner);
        when(projectRepository.findByIdAndOwnerId(99L, owner.getId())).thenReturn(Optional.empty());
        when(projectRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> projectService.findById(99L));
    }

    @Test
    void shouldThrowUnauthorizedWhenProjectBelongsToAnotherUser() {
        when(userService.getAuthenticatedUser()).thenReturn(owner);
        when(projectRepository.findByIdAndOwnerId(project.getId(), owner.getId())).thenReturn(Optional.empty());
        when(projectRepository.existsById(project.getId())).thenReturn(true);

        assertThrows(UnauthorizedAccessException.class, () -> projectService.findById(project.getId()));
    }

    @Test
    void shouldUpdateProjectSuccessfully() {
        UpdateProjectRequest request = new UpdateProjectRequest("Updated Name", "Updated desc");
        when(userService.getAuthenticatedUser()).thenReturn(owner);
        when(projectRepository.findByIdAndOwnerId(project.getId(), owner.getId()))
                .thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponse response = projectService.update(project.getId(), request);

        assertNotNull(response);
        verify(projectRepository).save(project);
    }

    @Test
    void shouldDeleteProjectSuccessfully() {
        when(userService.getAuthenticatedUser()).thenReturn(owner);
        when(projectRepository.findByIdAndOwnerId(project.getId(), owner.getId()))
                .thenReturn(Optional.of(project));

        assertDoesNotThrow(() -> projectService.delete(project.getId()));
        verify(projectRepository).delete(project);
    }

    @Test
    void shouldNotDeleteProjectOfAnotherUser() {
        when(userService.getAuthenticatedUser()).thenReturn(owner);
        when(projectRepository.findByIdAndOwnerId(project.getId(), owner.getId())).thenReturn(Optional.empty());
        when(projectRepository.existsById(project.getId())).thenReturn(true);

        assertThrows(UnauthorizedAccessException.class, () -> projectService.delete(project.getId()));
        verify(projectRepository, never()).delete(any());
    }
}
