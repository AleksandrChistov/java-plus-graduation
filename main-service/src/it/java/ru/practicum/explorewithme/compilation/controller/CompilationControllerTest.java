package ru.practicum.explorewithme.compilation.controller;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.explorewithme.compilation.model.Compilation;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("local")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
class CompilationControllerTest {

    private final EntityManager em;

    private MockMvc mvc;

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoCompilationsExist() throws Exception {
        mvc.perform(get(CompilationController.URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAll_shouldReturnAllCompilations_whenNoParametersProvided() throws Exception {
        persistCompilation("Первая подборка", false);
        persistCompilation("Вторая подборка", true);
        persistCompilation("Третья подборка", false);

        em.flush();

        mvc.perform(get(CompilationController.URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void getAll_shouldReturnPinnedCompilations_whenPinnedIsTrue() throws Exception {
        persistCompilation("Первая подборка", false);
        persistCompilation("Вторая подборка", true);
        persistCompilation("Третья подборка", false);

        em.flush();

        mvc.perform(get(CompilationController.URL + "?pinned=true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Вторая подборка")))
                .andExpect(jsonPath("$[0].pinned", is(true)));
    }

    @Test
    void getAll_shouldReturnPinnedCompilations_whenPinnedIsFalse() throws Exception {
        persistCompilation("Первая подборка", false);
        persistCompilation("Вторая подборка", true);
        persistCompilation("Третья подборка", false);

        em.flush();

        mvc.perform(get(CompilationController.URL + "?pinned=false"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Первая подборка")))
                .andExpect(jsonPath("$[0].pinned", is(false)))
                .andExpect(jsonPath("$[1].title", is("Третья подборка")))
                .andExpect(jsonPath("$[1].pinned", is(false)));
    }


    @Test
    void getAll_shouldRespectPaginationParameters() throws Exception {
        for (int i = 1; i <= 10; i++) {
            persistCompilation("Подборка " + i, i % 2 == 0);
        }

        mvc.perform(get(CompilationController.URL + "?from=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].title", is("Подборка 1")))
                .andExpect(jsonPath("$[4].title", is("Подборка 5")));

        mvc.perform(get(CompilationController.URL + "?from=5&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].title", is("Подборка 6")))
                .andExpect(jsonPath("$[4].title", is("Подборка 10")));
    }

    @Test
    void getAll_shouldReturnBadRequest_whenFromIsNegative() throws Exception {
        mvc.perform(get(CompilationController.URL + "?from=-1&size=5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_shouldReturnBadRequest_whenSizeIsInvalid() throws Exception {
        mvc.perform(get(CompilationController.URL + "?from=0&size=-5"))
                .andExpect(status().isBadRequest());

        mvc.perform(get(CompilationController.URL + "?from=0&size=0"))
                .andExpect(status().isBadRequest());
    }

    private void persistCompilation(String title, Boolean pinned) {
        Compilation compilation1 = new Compilation();
        compilation1.setTitle(title);
        compilation1.setPinned(pinned);
        em.persist(compilation1);
    }

}