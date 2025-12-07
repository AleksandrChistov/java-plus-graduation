package ru.practicum.explorewithme.compilation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.explorewithme.compilation.dto.CreateCompilationDto;
import ru.practicum.explorewithme.compilation.dto.ResponseCompilationDto;
import ru.practicum.explorewithme.compilation.dto.UpdateCompilationDto;
import ru.practicum.explorewithme.compilation.model.Compilation;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
class AdminCompilationControllerTest {

    private final ObjectMapper objectMapper;

    private final EntityManager em;

    private MockMvc mvc;

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void create_shouldCreateCompilation_whenValidDataProvided() throws Exception {
        CreateCompilationDto requestCompilationDto = new CreateCompilationDto("Новая подборка",  false, Set.of());

        MvcResult result = mvc.perform(post(AdminCompilationController.URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(requestCompilationDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResponseCompilationDto response = objectMapper.readValue(content, ResponseCompilationDto.class);

        assertNotNull(response.getId());
        assertEquals("Новая подборка", response.getTitle());
        assertFalse(response.getPinned());
        assertEquals(0, response.getEvents().size());
    }

    @Test
    void create_shouldReturnBadRequest_whenTitleIsBlank() throws Exception {
        CreateCompilationDto requestCompilationDto = new CreateCompilationDto();
        requestCompilationDto.setTitle("");

        mvc.perform(post(AdminCompilationController.URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(requestCompilationDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturnBadRequest_whenTitleIsNull() throws Exception {
        CreateCompilationDto requestCompilationDto = new CreateCompilationDto();
        requestCompilationDto.setTitle("");

        mvc.perform(post(AdminCompilationController.URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(requestCompilationDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturnConflict_whenTitleIsNotUnique() throws Exception {
        Compilation compilation = new Compilation();
        compilation.setTitle("Первая подборка");

        em.persist(compilation);
        em.flush();

        CreateCompilationDto requestCompilationDto = new CreateCompilationDto();
        requestCompilationDto.setTitle("Первая подборка");

        mvc.perform(post(AdminCompilationController.URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(requestCompilationDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void update_shouldUpdateCompilation_whenValidDataProvided() throws Exception {
        Compilation compilation = new Compilation();
        compilation.setTitle("Первая Подборка");
        compilation.setPinned(false);
        compilation.setEvents(Set.of());

        em.persist(compilation);
        em.flush();

        UpdateCompilationDto updateCompilationDto = new UpdateCompilationDto("Новая подборка",  true, Set.of());

        MvcResult result = mvc.perform(patch(AdminCompilationController.URL + "/{compId}", compilation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(updateCompilationDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResponseCompilationDto response = objectMapper.readValue(content, ResponseCompilationDto.class);

        assertNotNull(response.getId());
        assertEquals("Новая подборка", response.getTitle());
        assertTrue(response.getPinned());

        List<Compilation> compilations = em.createQuery("select c from Compilation  c", Compilation.class).getResultList();

        assertEquals(1, compilations.size());
    }

    @Test
    void update_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        Compilation compilation = new Compilation();
        compilation.setTitle("Первая подборка");

        em.persist(compilation);
        em.flush();

        UpdateCompilationDto updateCompilationDto = new UpdateCompilationDto();
        updateCompilationDto.setTitle("");

        mvc.perform(patch(AdminCompilationController.URL + "/{compId}", compilation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(updateCompilationDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnNotFound_whenCompilationDoesNotExist() throws Exception {
        UpdateCompilationDto updateCompilationDto = new UpdateCompilationDto("Новая подборка", true, Set.of());

        mvc.perform(patch(AdminCompilationController.URL + "/{compId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(updateCompilationDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturnConflict_whenTitleIsNotUnique() throws Exception {
        Compilation compilation1 = new Compilation();
        compilation1.setTitle("Первая подборка");
        em.persist(compilation1);

        Compilation compilation2 = new Compilation();
        compilation2.setTitle("Вторая подборка");
        em.persist(compilation2);

        em.flush();

        // Коммитим текущую транзакцию
        TestTransaction.flagForCommit(); // Помечаем для коммита
        TestTransaction.end(); // Выполняем коммит

        UpdateCompilationDto updateCompilationDto = new UpdateCompilationDto(compilation2.getTitle(), true, Set.of());

        mvc.perform(patch(AdminCompilationController.URL + "/{compId}", compilation1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(updateCompilationDto)))
                .andExpect(status().isConflict());

        // Транзакция для очистки БД
        TestTransaction.start();
        em.createQuery("delete from Compilation").executeUpdate();
        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

    @Test
    void delete_shouldDeleteCompilation_whenCompilationExists() throws Exception {
        Compilation compilation1 = new Compilation();
        compilation1.setTitle("Первая подборка");
        em.persist(compilation1);
        em.flush();

        List<Compilation> compilationsBefore = em.createQuery("select c from Compilation c", Compilation.class).getResultList();
        assertEquals(1, compilationsBefore.size());

        mvc.perform(delete(AdminCompilationController.URL + "/{compId}", compilation1.getId()))
                .andExpect(status().isNoContent());

        List<Compilation> compilationsAfter = em.createQuery("select c from Compilation c", Compilation.class).getResultList();
        assertEquals(0, compilationsAfter.size());
    }

    @Test
    void delete_shouldReturnNotFound_whenCompilationDoesNotExist() throws Exception {
        mvc.perform(delete(AdminCompilationController.URL + "/{compId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturnBadRequest_whenInvalidCompIdProvided() throws Exception {
        mvc.perform(delete(AdminCompilationController.URL + "/{compId}", -1L))
                .andExpect(status().isBadRequest());

        mvc.perform(delete(AdminCompilationController.URL + "/{compId}", 0L))
                .andExpect(status().isBadRequest());

        mvc.perform(delete(AdminCompilationController.URL + "/{compId}", "wrongType"))
                .andExpect(status().isBadRequest());
    }
}