package com.practice.taskmanager.dto;

import com.practice.taskmanager.model.Priority;
import com.practice.taskmanager.model.Task;

/**
 * DTO de salida. Mapea desde la entidad Task con un metodo estatico "from".
 */
public record TaskResponse(
        Long id,
        String title,
        boolean completed,
        Priority priority
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.isCompleted(), task.getPriority());
    }
}
