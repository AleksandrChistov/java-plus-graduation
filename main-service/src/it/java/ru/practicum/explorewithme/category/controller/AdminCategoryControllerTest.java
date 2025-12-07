package ru.practicum.explorewithme.category.controller;

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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.explorewithme.category.dto.RequestCategoryDto;
import ru.practicum.explorewithme.category.model.Category;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("local")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
class AdminCategoryControllerTest {

    private final ObjectMapper objectMapper;

    private final EntityManager em;

    private MockMvc mvc;

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void create_shouldCreateCategory_whenValidDataProvided() throws Exception {
        RequestCategoryDto requestCategoryDto = new RequestCategoryDto("Новая категория");

        mvc.perform(post(AdminCategoryController.URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(requestCategoryDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("Новая категория"));
    }

    @Test
    void create_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        RequestCategoryDto requestCategoryDto = new RequestCategoryDto("");

        mvc.perform(post(AdminCategoryController.URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(requestCategoryDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturnBadRequest_whenNameIsNull() throws Exception {
        RequestCategoryDto requestCategoryDto = new RequestCategoryDto(null);

        mvc.perform(post(AdminCategoryController.URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(requestCategoryDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturnConflict_whenNameIsNotUnique() throws Exception {
        Category category = new Category();
        category.setName("Первая категория");

        em.persist(category);
        em.flush();

        RequestCategoryDto requestCategoryDto = new RequestCategoryDto("Первая категория");

        mvc.perform(post(AdminCategoryController.URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(requestCategoryDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void update_shouldUpdateCategory_whenValidDataProvided() throws Exception {
        Category category = new Category();
        category.setName("Первая категория");

        em.persist(category);
        em.flush();

        RequestCategoryDto updateCategoryDto = new RequestCategoryDto("Новая категория");

        mvc.perform(patch(AdminCategoryController.URL + "/{catId}", category.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(updateCategoryDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("Новая категория"));

        List<Category> categories = em.createQuery("select c from Category c", Category.class).getResultList();

        assertEquals(1, categories.size());
    }

    @Test
    void update_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        Category category = new Category();
        category.setName("Первая категория");

        em.persist(category);
        em.flush();

        RequestCategoryDto updateCategoryDto = new RequestCategoryDto("");

        mvc.perform(patch(AdminCategoryController.URL + "/{catId}", category.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(updateCategoryDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnNotFound_whenCategoryDoesNotExist() throws Exception {
        RequestCategoryDto updateCategoryDto = new RequestCategoryDto("Новая категория");

        mvc.perform(patch(AdminCategoryController.URL + "/{catId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(updateCategoryDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturnConflict_whenNameIsNotUnique() throws Exception {
        Category category1 = new Category();
        category1.setName("Первая категория");
        em.persist(category1);

        Category category2 = new Category();
        category2.setName("Вторая категория");
        em.persist(category2);

        em.flush();

        // Коммитим текущую транзакцию
        TestTransaction.flagForCommit(); // Помечаем для коммита
        TestTransaction.end(); // Выполняем коммит

        RequestCategoryDto updateCategoryDto = new RequestCategoryDto(category2.getName());

        mvc.perform(patch(AdminCategoryController.URL + "/{catId}", category1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(updateCategoryDto)))
                .andExpect(status().isConflict());

        // Транзакция для очистки БД
        TestTransaction.start();
        em.createQuery("delete from Category").executeUpdate();
        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

    @Test
    void delete_shouldDeleteCategory_whenCategoryExists() throws Exception {
        Category category1 = new Category();
        category1.setName("Первая категория");
        em.persist(category1);
        em.flush();

        List<Category> categoriesBefore = em.createQuery("select c from Category c", Category.class).getResultList();
        assertEquals(1, categoriesBefore.size());

        mvc.perform(delete(AdminCategoryController.URL + "/{catId}", category1.getId()))
                .andExpect(status().isNoContent());

        List<Category> categoriesAfter = em.createQuery("select c from Category c", Category.class).getResultList();
        assertEquals(0, categoriesAfter.size());
    }

    @Test
    void delete_shouldReturnNotFound_whenCategoryDoesNotExist() throws Exception {
        mvc.perform(delete(AdminCategoryController.URL + "/{catId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturnBadRequest_whenInvalidCatIdProvided() throws Exception {
        mvc.perform(delete(AdminCategoryController.URL + "/{catId}", -1L))
                .andExpect(status().isBadRequest());

        mvc.perform(delete(AdminCategoryController.URL + "/{catId}", 0L))
                .andExpect(status().isBadRequest());

        mvc.perform(delete(AdminCategoryController.URL + "/{catId}", "wrongType"))
                .andExpect(status().isBadRequest());
    }
}