package com.practice.taskmanager.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test de integracion: @SpringBootTest levanta el contexto COMPLETO de Spring
 * (Controller -> Service -> Repository -> H2 en memoria), igual que en produccion
 * pero con la DB en memoria definida en application.properties.
 *
 * MockMvc simula requests HTTP sin levantar un servidor real en un puerto.
 *
 * Diferencia con TaskServiceImplTest:
 * - Aquel test mockeaba el Repository (rapido, aislado, prueba SOLO la logica del Service).
 * - Este prueba el flujo end-to-end real, incluyendo serializacion JSON, validacion
 *   y mapeo JPA contra una base de datos real (aunque sea en memoria).
 */
@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void crearTask_yLuegoListarla() throws Exception {
        String body = """
                {"title": "Repasar SOLID", "priority": "HIGH"}
                """;

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Repasar SOLID")))
                .andExpect(jsonPath("$.completed", is(false)));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk());
    }

    @Test
    void crearTask_conTitleVacio_devuelve400() throws Exception {
        String body = """
                {"title": "", "priority": "LOW"}
                """;

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscarTaskInexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/api/tasks/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
}
