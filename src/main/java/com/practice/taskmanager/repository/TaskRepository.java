package com.practice.taskmanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.practice.taskmanager.model.Priority;
import com.practice.taskmanager.model.Task;

/**
 * Repository = capa de acceso a datos.
 *
 * JpaRepository<Task, Long> ya provee, sin escribir nada:
 *   save(), findById(), findAll(), deleteById(), existsById(), count()...
 *
 * Hibernate genera el SQL real (SELECT/INSERT/UPDATE/DELETE) detras de esta interface.
 *
 * "Query methods": Spring Data parsea el NOMBRE del metodo y genera la query.
 * findByCompleted(false) -> SELECT * FROM task WHERE completed = false
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByCompleted(boolean completed);

    List<Task> findByPriority(Priority priority);
}
