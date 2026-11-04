package org.example.tasks;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration-style controller tests exercise HTTP, validation and persistence.
 * Each test rolls back its transaction so records cannot leak into another test.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@Transactional
class TaskControllerTest {
    @Autowired
    MockMvc mvc;

    @Test
    void createAndRetrieveTask() throws Exception {
        String body = "{\"title\":\"Review filing\",\"status\":\"TODO\",\"dueAt\":\"2027-01-01T12:00:00Z\"}";
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Review filing"));
        mvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Review filing"));
    }

    @Test
    void invalidTaskIsRejected() throws Exception {
        // Both a blank title and missing due date violate the create contract.
        mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"status\":\"TODO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingTaskReturns404() throws Exception {
        mvc.perform(get("/api/tasks/999999")).andExpect(status().isNotFound());
    }

    @Test
    void changeStatusAndDelete() throws Exception {
        String body = "{\"title\":\"Review filing\",\"status\":\"TODO\",\"dueAt\":\"2027-01-01T12:00:00Z\"}";
        String location = mvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse().getHeader("Location");

        // Follow the resource URI returned by POST instead of assuming an ID.
        mvc.perform(patch(location + "/status")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
        mvc.perform(delete(location)).andExpect(status().isNoContent());
        mvc.perform(get(location)).andExpect(status().isNotFound());
    }
}
