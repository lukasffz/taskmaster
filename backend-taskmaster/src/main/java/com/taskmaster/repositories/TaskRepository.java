package com.taskmaster.repositories;

import com.taskmaster.models.Task;
import com.taskmaster.models.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<Task> findByProjectIdAndStatusOrderByCreatedAtDesc(Long projectId, TaskStatus status);

    Optional<Task> findByIdAndProjectId(Long id, Long projectId);

    boolean existsByIdAndProjectId(Long id, Long projectId);
}
