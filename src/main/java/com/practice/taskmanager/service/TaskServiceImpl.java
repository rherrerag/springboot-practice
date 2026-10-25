package com.practice.taskmanager.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.practice.taskmanager.dto.TaskRequest;
import com.practice.taskmanager.dto.TaskResponse;
import com.practice.taskmanager.exception.TaskNotFoundException;
import com.practice.taskmanager.model.Task;
import com.practice.taskmanager.repository.TaskRepository;

/**
 * @Service marca esta clase como un bean de la capa de negocio.
 * Spring la instancia una vez (singleton) y la inyecta donde se necesite.
 *
 * Dependency Injection (DI):
 * - El constructor recibe TaskRepository. Spring ve que TaskRepository es una interface
 *   con un bean disponible (el proxy que genera Spring Data JPA) y lo inyecta solo.
 * - Esta clase nunca hace "new TaskRepositoryImpl()": no conoce la implementacion concreta,
 *   ni necesita conocerla. Eso es Inversion of Control.
 * - Constructor injection (vs @Autowired en un campo) permite que el campo sea "final"
 *   y que el objeto sea inmutable/testeable (se puede pasar un mock en el constructor).
 */
@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public List<TaskResponse> findAll() {
        return taskRepository.findAll().stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Override
    public TaskResponse findById(Long id) {
        Task task = findEntityOrThrow(id);
        return TaskResponse.from(task);
    }

    @Override
    public TaskResponse create(TaskRequest request) {
        Task task = new Task(request.title(), request.priority());
        return TaskResponse.from(taskRepository.save(task));
    }

    @Override
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = findEntityOrThrow(id);
        task.setTitle(request.title());
        task.setPriority(request.priority());
        return TaskResponse.from(taskRepository.save(task));
    }

    @Override
    public TaskResponse complete(Long id) {
        Task task = findEntityOrThrow(id);
        task.setCompleted(true);
        return TaskResponse.from(taskRepository.save(task));
    }

    @Override
    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }

    @Override
    public List<TaskResponse> findByCompleted(boolean completed) {
        return taskRepository.findByCompleted(completed).stream()
                .map(TaskResponse::from)
                .toList();
    }

    private Task findEntityOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }
}
