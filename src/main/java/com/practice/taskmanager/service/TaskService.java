package com.practice.taskmanager.service;

import java.util.List;

import com.practice.taskmanager.dto.TaskRequest;
import com.practice.taskmanager.dto.TaskResponse;

/**
 * Interface de servicio.
 *
 * Por que una interface y no solo la clase?
 * - DIP (principio D de SOLID): el Controller depende de esta abstraccion,
 *   no de TaskServiceImpl. Se podria reemplazar la implementacion (por ejemplo
 *   una version que use cache) sin tocar el Controller.
 * - Facilita testear el Controller con un mock de TaskService (sin DB real).
 */
public interface TaskService {

    List<TaskResponse> findAll();

    TaskResponse findById(Long id);

    TaskResponse create(TaskRequest request);

    TaskResponse update(Long id, TaskRequest request);

    TaskResponse complete(Long id);

    void delete(Long id);

    List<TaskResponse> findByCompleted(boolean completed);
}
