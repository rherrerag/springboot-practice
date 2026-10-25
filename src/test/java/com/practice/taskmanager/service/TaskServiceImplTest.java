package com.practice.taskmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.practice.taskmanager.dto.TaskRequest;
import com.practice.taskmanager.dto.TaskResponse;
import com.practice.taskmanager.exception.TaskNotFoundException;
import com.practice.taskmanager.model.Priority;
import com.practice.taskmanager.model.Task;
import com.practice.taskmanager.repository.TaskRepository;

/**
 * Test unitario "puro": no levanta Spring (no @SpringBootTest), no toca la DB.
 *
 * @ExtendWith(MockitoExtension.class) activa el procesamiento de @Mock.
 * @Mock crea un TaskRepository falso, que se inyecta a mano en el constructor de
 * TaskServiceImpl (la misma DI que hace Spring, pero manual y controlada por el test).
 *
 * Esto es posible justamente porque TaskServiceImpl depende de la INTERFACE
 * TaskRepository (DIP), no de una implementacion concreta atada a una DB real.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Test
    void findById_devuelveLaTaskCuandoExiste() {
        Task task = new Task("Estudiar Spring", Priority.HIGH);
        task.setId(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskServiceImpl service = new TaskServiceImpl(taskRepository);
        TaskResponse response = service.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Estudiar Spring");
        assertThat(response.priority()).isEqualTo(Priority.HIGH);
    }

    @Test
    void findById_lanzaExcepcionCuandoNoExiste() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        TaskServiceImpl service = new TaskServiceImpl(taskRepository);

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_guardaUnaNuevaTaskNoCompletada() {
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        TaskServiceImpl service = new TaskServiceImpl(taskRepository);
        TaskResponse response = service.create(new TaskRequest("Aprender JPA", Priority.MEDIUM));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.completed()).isFalse();
        assertThat(response.priority()).isEqualTo(Priority.MEDIUM);
    }

    @Test
    void complete_marcaLaTaskComoCompletada() {
        Task task = new Task("Lavar los platos", Priority.LOW);
        task.setId(5L);
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskServiceImpl service = new TaskServiceImpl(taskRepository);
        TaskResponse response = service.complete(5L);

        assertThat(response.completed()).isTrue();
    }

    @Test
    void findByCompleted_filtraPorEstado() {
        Task done = new Task("Tarea hecha", Priority.LOW);
        done.setCompleted(true);
        when(taskRepository.findByCompleted(true)).thenReturn(List.of(done));

        TaskServiceImpl service = new TaskServiceImpl(taskRepository);
        List<TaskResponse> result = service.findByCompleted(true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).completed()).isTrue();
    }
}
