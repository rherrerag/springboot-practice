package com.practice.taskmanager.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.practice.taskmanager.dto.TaskRequest;
import com.practice.taskmanager.dto.TaskResponse;
import com.practice.taskmanager.service.TaskService;

import jakarta.validation.Valid;

/**
 * @RestController = @Controller + @ResponseBody: cada metodo devuelve el body
 * de la respuesta (Spring lo serializa a JSON con Jackson), no el nombre de una vista.
 *
 * @RequestMapping("/api/tasks") fija el prefijo comun de todas las rutas de esta clase.
 *
 * El Controller es la capa mas "delgada": valida la forma del request (@Valid),
 * delega TODA la logica de negocio en TaskService y traduce el resultado a HTTP
 * (codigo de estado + body). No habla con el Repository directamente.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    // Constructor injection: Spring inyecta la implementacion (TaskServiceImpl)
    // a traves de la interface TaskService.
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponse> findAll(@RequestParam(required = false) Boolean completed) {
        if (completed != null) {
            return taskService.findByCompleted(completed);
        }
        return taskService.findAll();
    }

    @GetMapping("/{id}")
    public TaskResponse findById(@PathVariable Long id) {
        return taskService.findById(id);
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        TaskResponse created = taskService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return taskService.update(id, request);
    }

    @PatchMapping("/{id}/complete")
    public TaskResponse complete(@PathVariable Long id) {
        return taskService.complete(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
