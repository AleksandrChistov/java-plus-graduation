package ru.practicum.explorewithme.compilation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.client.RecommendationsClient;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.explorewithme.api.request.enums.RequestStatus;
import ru.practicum.explorewithme.api.user.dto.UserDto;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.compilation.dto.CreateCompilationDto;
import ru.practicum.explorewithme.compilation.dto.ResponseCompilationDto;
import ru.practicum.explorewithme.compilation.dto.UpdateCompilationDto;
import ru.practicum.explorewithme.compilation.model.Compilation;
import ru.practicum.explorewithme.event.client.request.RequestClient;
import ru.practicum.explorewithme.event.client.user.UserClient;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.Location;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.config.location=classpath:application-test.yml"
})
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
class AdminCompilationControllerTest {

    private MockMvc mvc;

    private final ObjectMapper objectMapper;

    private final EntityManager em;

    @MockBean
    private RequestClient requestClient;

    @MockBean
    private final UserClient userClient;

    @MockBean
    private final RecommendationsClient recommendationsClient;

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        em.clear();
        this.mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void create_shouldCreateCompilation_whenValidDataProvided() throws Exception {
        Event event1 = Event.builder()
                .title("Название события 1")
                .annotation("Аннотация события 1")
                .description("Описание события 1")
                .location(new Location(12.12F, 44.12F))
                .categoryId(1L)
                .eventDate(LocalDateTime.of(2025, 12, 17, 11, 0))
                .paid(false)
                .initiatorId(1L)
                .build();

        Event event2 = Event.builder()
                .title("Название события 2")
                .annotation("Аннотация события 2")
                .description("Описание события 2")
                .location(new Location(12.12F, 44.12F))
                .categoryId(1L)
                .eventDate(LocalDateTime.of(2025, 12, 17, 12, 0))
                .paid(true)
                .initiatorId(1L)
                .build();

        Category category = Category.builder()
                .name("Категория 1")
                .build();

        em.persist(category);
        em.persist(event1);
        em.persist(event2);
        em.flush();

        Long eventId1 = 1L;
        Long eventId2 = 2L;

        when(userClient.getAllByIds(anySet()))
                .thenReturn(List.of(new UserDto(1L, "Иванов Иван", "ivanov@mail.ru")));

        when(recommendationsClient.getInteractionsCount(anySet(), anyInt()))
                .thenReturn(
                        Stream.of(
                                RecommendedEventProto.newBuilder()
                                        .setEventId(eventId1)
                                        .setScore(0.4)
                                        .build(),
                                RecommendedEventProto.newBuilder()
                                        .setEventId(eventId2)
                                        .setScore(0.8)
                                        .build()
                        )
                );

        when(requestClient.getRequestsCountsByStatusAndEventIds(eq(RequestStatus.CONFIRMED), anySet()))
                .thenReturn(Map.of(eventId1, 10L, eventId2, 0L));

        CreateCompilationDto requestCompilationDto = new CreateCompilationDto("Новая подборка", null, Set.of(eventId1, eventId2));

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
        assertEquals(false, response.getPinned());
        assertEquals(2, response.getEvents().size());

        Compilation fromDb = em.find(Compilation.class, response.getId());

        assertNotNull(fromDb.getId());
        assertEquals("Новая подборка", fromDb.getTitle());
        assertEquals(false, fromDb.getPinned());
        assertEquals(2, fromDb.getEventIds().size());
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
        requestCompilationDto.setTitle(null);

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
        compilation.setEventIds(Set.of());

        em.persist(compilation);
        em.flush();

        UpdateCompilationDto updateCompilationDto = new UpdateCompilationDto("Новая подборка", true, Set.of());

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