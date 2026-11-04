package org.example.tasks;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data supplies the CRUD operations; no custom SQL is required here. */
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {}
