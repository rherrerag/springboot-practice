package com.practice.taskmanager.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * @Entity le dice a Hibernate (JPA) que esta clase mapea a una tabla.
 * Cada instancia = una fila de la tabla "task" (nombre derivado de la clase).
 * Cada campo = una columna.
 */
@Entity
public class Task {

    /**
     * @Id marca la clave primaria.
     * @GeneratedValue(IDENTITY) delega en la DB la generacion del id (auto-increment).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private boolean completed;

    /**
     * @Enumerated(STRING) guarda el nombre del enum ("HIGH") en vez del indice (2).
     * Mas legible en la DB y a prueba de reordenamientos del enum.
     */
    @Enumerated(EnumType.STRING)
    private Priority priority;

    // JPA exige un constructor vacio: lo usa para reconstruir entidades desde la DB via reflexion.
    public Task() {
    }

    public Task(String title, Priority priority) {
        this.title = title;
        this.priority = priority;
        this.completed = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}
