package com.practice.taskmanager.dto;

import com.practice.taskmanager.model.Priority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada. No se reutiliza la @Entity Task directamente como request:
 * separa "lo que viaja por la API" de "lo que se persiste" (pueden evolucionar distinto,
 * y evita exponer campos internos como el id).
 */
public record TaskRequest(
        @NotBlank(message = "title no puede estar vacio") String title,
        @NotNull(message = "priority es obligatorio") Priority priority
) {
}
