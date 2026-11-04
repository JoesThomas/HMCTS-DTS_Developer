package org.example.tasks;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/** Persisted task record; field lengths mirror the create-request validation. */
@Entity
@Table(name = "tasks")
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 150)
    public String title;

    @Column(length = 2000)
    public String description;

    // Persist enum names rather than ordinals so reordering values cannot change their meaning.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public TaskStatus status;

    // OffsetDateTime makes the submitted time zone offset explicit at the API boundary.
    @Column(nullable = false)
    public OffsetDateTime dueAt;

    // JPA requires a no-argument constructor to materialise database records.
    protected TaskEntity() {}

    public TaskEntity(String title, String description, TaskStatus status, OffsetDateTime dueAt) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueAt = dueAt;
    }
}
