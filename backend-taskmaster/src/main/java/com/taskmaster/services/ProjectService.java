package com.taskmaster.services;

import com.taskmaster.dtos.project.CreateProjectRequest;
import com.taskmaster.dtos.project.ProjectResponse;
import com.taskmaster.dtos.project.UpdateProjectRequest;
import com.taskmaster.exceptions.ResourceNotFoundException;
import com.taskmaster.exceptions.UnauthorizedAccessException;
import com.taskmaster.models.Project;
import com.taskmaster.models.User;
import com.taskmaster.repositories.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final UserService userService;

    public ProjectService(ProjectRepository projectRepository, UserService userService) {
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    @Transactional
    public ProjectResponse create(CreateProjectRequest request) {
        User owner = userService.getAuthenticatedUser();

        Project project = new Project(request.getName(), request.getDescription(), owner);
        Project saved = projectRepository.save(project);

        log.info("Project created: id={}, owner={}", saved.getId(), owner.getId());
        return ProjectResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listAllForCurrentUser() {
        User owner = userService.getAuthenticatedUser();
        return projectRepository.findByOwnerIdOrderByCreatedAtDesc(owner.getId())
                .stream()
                .map(ProjectResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse findById(Long projectId) {
        User owner = userService.getAuthenticatedUser();
        Project project = resolveOwnedProject(projectId, owner.getId());
        return ProjectResponse.fromEntity(project);
    }

    @Transactional
    public ProjectResponse update(Long projectId, UpdateProjectRequest request) {
        User owner = userService.getAuthenticatedUser();
        Project project = resolveOwnedProject(projectId, owner.getId());

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        Project saved = projectRepository.save(project);
        log.info("Project updated: id={}, owner={}", saved.getId(), owner.getId());
        return ProjectResponse.fromEntity(saved);
    }

    @Transactional
    public void delete(Long projectId) {
        User owner = userService.getAuthenticatedUser();
        Project project = resolveOwnedProject(projectId, owner.getId());

        projectRepository.delete(project);
        log.info("Project deleted: id={}, owner={}", projectId, owner.getId());
    }

    /**
     * Resolves a project by id and verifies ownership.
     * Throws ResourceNotFoundException if not found, UnauthorizedAccessException if the
     * authenticated user is not the owner. This prevents IDOR: callers never learn
     * whether a project belongs to another user or simply does not exist.
     */
    private Project resolveOwnedProject(Long projectId, Long ownerId) {
        return projectRepository.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> {
                    // Check if the project exists at all to decide the correct error.
                    // We use existsById so there is no extra fetch of the full entity.
                    if (projectRepository.existsById(projectId)) {
                        throw new UnauthorizedAccessException(
                                "Access denied to project with id: " + projectId);
                    }
                    return new ResourceNotFoundException(
                            "Project not found with id: " + projectId);
                });
    }
}
