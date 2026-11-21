package com.example.taskmanager.controller;

import com.example.taskmanager.dto.StatusUpdateRequest;
import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Status;
import com.example.taskmanager.model.User;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@AuthenticationPrincipal User user, @Valid @RequestBody TaskRequest req) {
        return taskService.create(user, req);
    }

    @GetMapping
    public Page<TaskResponse> list(@AuthenticationPrincipal User user,
                                   @RequestParam(required = false) Status status,
                                   @RequestParam(required = false) Priority priority,
                                   @RequestParam(required = false) String q,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(defaultValue = "createdAt") String sortBy,
                                   @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = Sort.by("asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy.matches("title|status|priority|dueDate|createdAt|updatedAt") ? sortBy : "createdAt");
        return taskService.search(user, status, priority, q,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), sort));
    }

    @GetMapping("/stats")
    public Map<String, Long> stats(@AuthenticationPrincipal User user) {
        return taskService.stats(user);
    }

    @GetMapping("/{id}")
    public TaskResponse get(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return taskService.get(user, id);
    }

    @PutMapping("/{id}")
    public TaskResponse update(@AuthenticationPrincipal User user, @PathVariable Long id,
                               @Valid @RequestBody TaskRequest req) {
        return taskService.update(user, id, req);
    }

    @PatchMapping("/{id}/status")
    public TaskResponse updateStatus(@AuthenticationPrincipal User user, @PathVariable Long id,
                                     @Valid @RequestBody StatusUpdateRequest req) {
        return taskService.updateStatus(user, id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        taskService.delete(user, id);
    }
}
