package org.example.tasks;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * HTTP endpoints for the caseworker task workflow.
 *
 * The frontend's development origin is explicitly allowed for local development;
 * production deployments should configure CORS for their actual frontend origin.
 */
@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {
    private final TaskRepository repo;

    public TaskController(TaskRepository repo) {
        this.repo = repo;
    }

    /**
     * Input contract for creating a task. Bean Validation rejects incomplete
     * requests before a record is persisted; dueAt includes a UTC offset.
     */
    public record NewTask(
            @NotBlank @Size(max = 150) String title,
            @Size(max = 2000) String description,
            @NotNull TaskStatus status,
            @NotNull OffsetDateTime dueAt) {}

    /** Restrict status updates to the status field rather than replacing a task. */
    public record StatusChange(@NotNull TaskStatus status) {}

    @GetMapping
    public List<TaskEntity> all() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public TaskEntity one(@PathVariable long id) {
        return find(id);
    }

    /** Return 201 Created with the new resource URI in the Location header. */
    @PostMapping
    public ResponseEntity<TaskEntity> create(@Valid @RequestBody NewTask input) {
        TaskEntity saved = repo.save(new TaskEntity(
                input.title().trim(), input.description(), input.status(), input.dueAt()));
        return ResponseEntity.created(URI.create("/api/tasks/" + saved.id)).body(saved);
    }

    @PatchMapping("/{id}/status")
    public TaskEntity changeStatus(@PathVariable long id, @Valid @RequestBody StatusChange input) {
        TaskEntity task = find(id);
        task.status = input.status();
        return repo.save(task);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        TaskEntity task = find(id);
        repo.delete(task);
    }

    /** Share lookup semantics so missing IDs consistently produce HTTP 404. */
    private TaskEntity find(long id) {
        return repo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }
}
