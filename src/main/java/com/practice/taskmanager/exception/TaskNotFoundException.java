package com.practice.taskmanager.exception;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long id) {
        super("Task no encontrada con id " + id);
    }
}
