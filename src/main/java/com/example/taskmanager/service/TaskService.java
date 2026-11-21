package com.example.taskmanager.service;

import com.example.taskmanager.dto.StatusUpdateRequest;
import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.exception.ApiException;
import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Status;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskResponse create(User user, TaskRequest req) {
        Task task = new Task();
        apply(task, req);
        task.setUser(user);
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> search(User user, Status status, Priority priority, String q, Pageable pageable) {
        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.equal(root.get("user").get("id"), user.getId()));
            if (status != null) p.add(cb.equal(root.get("status"), status));
            if (priority != null) p.add(cb.equal(root.get("priority"), priority));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                p.add(cb.or(cb.like(cb.lower(root.get("title")), like),
                            cb.like(cb.lower(root.get("description")), like)));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
        return taskRepository.findAll(spec, pageable).map(TaskResponse::from);
    }

    @Transactional(readOnly = true)
    public TaskResponse get(User user, Long id) {
        return TaskResponse.from(find(user, id));
    }

    public TaskResponse update(User user, Long id, TaskRequest req) {
        Task task = find(user, id);
        apply(task, req);
        return TaskResponse.from(task);
    }

    public TaskResponse updateStatus(User user, Long id, StatusUpdateRequest req) {
        Task task = find(user, id);
        task.setStatus(req.status());
        return TaskResponse.from(task);
    }

    public void delete(User user, Long id) {
        taskRepository.delete(find(user, id));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> stats(User user) {
        Map<String, Long> stats = new LinkedHashMap<>();
        long total = 0;
        for (Status s : Status.values()) {
            long c = taskRepository.countByUserIdAndStatus(user.getId(), s);
            stats.put(s.name(), c);
            total += c;
        }
        stats.put("TOTAL", total);
        stats.put("OVERDUE", taskRepository.countByUserIdAndStatusNotAndDueDateBefore(
                user.getId(), Status.DONE, LocalDate.now()));
        return stats;
    }

    private Task find(User user, Long id) {
        return taskRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Task not found with id " + id));
    }

    private void apply(Task task, TaskRequest req) {
        task.setTitle(req.title().trim());
        task.setDescription(req.description());
        if (req.status() != null) task.setStatus(req.status());
        if (req.priority() != null) task.setPriority(req.priority());
        task.setDueDate(req.dueDate());
    }
}
