package com.example.taskmanager.repository;

import com.example.taskmanager.model.Status;
import com.example.taskmanager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    Optional<Task> findByIdAndUserId(Long id, Long userId);
    long countByUserIdAndStatus(Long userId, Status status);
    long countByUserIdAndStatusNotAndDueDateBefore(Long userId, Status status, LocalDate date);
}
