package ru.practicum.explorewithme.event.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.event.comment.controller.AdminCommentController;
import ru.practicum.explorewithme.event.comment.dto.ResponseCommentDto;
import ru.practicum.explorewithme.event.comment.dto.UpdateCommentDto;
import ru.practicum.explorewithme.event.comment.enums.Status;
import ru.practicum.explorewithme.event.comment.model.Comment;
import ru.practicum.explorewithme.event.enums.State;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.Location;
import ru.practicum.explorewithme.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("local")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class AdminCommentControllerTest {

    private final ObjectMapper objectMapper;

    private final EntityManager em;

    private MockMvc mvc;

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void getAll_shouldReturnComments_whenNoStatusProvided() throws Exception {
        User user = createUser("testUser", "test@example.com");
        Category category = createCategory("Test Category");
        Location location = createLocation(55.75f, 37.62f);
        Event event = createEvent(user, category, location);
        Comment comment1 = createComment(user, event, "First comment");
        Comment comment2 = createComment(user, event, "Second comment");

        em.persist(user);
        em.persist(category);
        em.persist(event);
        em.persist(comment1);
        em.persist(comment2);
        em.flush();

        MvcResult result = mvc.perform(get(AdminCommentController.URL + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<ResponseCommentDto> response = objectMapper.readValue(content,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ResponseCommentDto.class));

        assertNotNull(response);
        assertEquals(2, response.size());
        assertTrue(response.stream().anyMatch(c -> c.getText().equals("First comment")));
        assertTrue(response.stream().anyMatch(c -> c.getText().equals("Second comment")));
    }

    @Test
    void getAll_shouldReturnCommentsFilteredByStatus() throws Exception {
        User user = createUser("testUser", "test@example.com");
        Category category = createCategory("Test Category");
        Location location = createLocation(55.75f, 37.62f);
        Event event = createEvent(user, category, location);
        Comment comment1 = createComment(user, event, "First comment");
        comment1.setStatus(Status.PUBLISHED);
        Comment comment2 = createComment(user, event, "Second comment");
        comment2.setStatus(Status.PENDING);

        em.persist(user);
        em.persist(category);
        em.persist(event);
        em.persist(comment1);
        em.persist(comment2);
        em.flush();

        MvcResult result = mvc.perform(get(AdminCommentController.URL + "/comments")
                        .param("status", Status.PUBLISHED.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<ResponseCommentDto> response = objectMapper.readValue(content,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ResponseCommentDto.class));

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("First comment", response.getFirst().getText());
        assertEquals(Status.PUBLISHED, response.getFirst().getStatus());
    }

    @Test
    void getAll_shouldReturnCommentsFilteredByFromAndSize() throws Exception {
        User user = createUser("testUser", "test@example.com");
        Category category = createCategory("Test Category");
        Location location = createLocation(55.75f, 37.62f);
        Event event = createEvent(user, category, location);
        Comment comment1 = createComment(user, event, "First comment");
        comment1.setStatus(Status.PUBLISHED);
        Comment comment2 = createComment(user, event, "Second comment");
        Thread.sleep(10);
        Comment comment3 = createComment(user, event, "3 comment");
        Thread.sleep(10);
        Comment comment4 = createComment(user, event, "4 comment");
        Thread.sleep(10);
        Comment comment5 = createComment(user, event, "5 comment");
        Thread.sleep(10);
        Comment comment6 = createComment(user, event, "6 comment");
        Thread.sleep(10);
        Comment comment7 = createComment(user, event, "7 comment");

        em.persist(user);
        em.persist(category);
        em.persist(event);
        em.persist(comment1);
        em.persist(comment2);
        em.persist(comment3);
        em.persist(comment4);
        em.persist(comment5);
        em.persist(comment6);
        em.persist(comment7);
        em.flush();

        MvcResult result = mvc.perform(get(AdminCommentController.URL + "/comments")
                        .param("from", "2")
                        .param("size", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<ResponseCommentDto> response = objectMapper.readValue(content,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ResponseCommentDto.class));

        assertNotNull(response);
        assertEquals(2, response.size());
        assertTrue(response.stream().anyMatch(c -> c.getText().equals("5 comment")));
        assertTrue(response.stream().anyMatch(c -> c.getText().equals("4 comment")));
    }

    @Test
    void getByEventId_shouldReturnCommentsForEvent() throws Exception {
        User user = createUser("testUser", "test@example.com");
        Category category = createCategory("Test Category");
        Location location = createLocation(55.75f, 37.62f);
        Event event1 = createEvent(user, category, location);
        Event event2 = createEvent(user, category, location);
        Comment comment1 = createComment(user, event1, "Event 1 comment");
        Comment comment2 = createComment(user, event2, "Event 2 comment");

        em.persist(user);
        em.persist(category);
        em.persist(event1);
        em.persist(event2);
        em.persist(comment1);
        em.persist(comment2);
        em.flush();

        MvcResult result = mvc.perform(get(AdminCommentController.URL + "/{eventId}/comments", event1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<ResponseCommentDto> response = objectMapper.readValue(content,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ResponseCommentDto.class));

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Event 1 comment", response.getFirst().getText());
        assertEquals(event1.getId(), response.getFirst().getEventId());
    }

    @Test
    void getByEventId_shouldReturnCommentsForEventFilteredByStatus() throws Exception {
        User user = createUser("testUser", "test@example.com");
        Category category = createCategory("Test Category");
        Location location = createLocation(55.75f, 37.62f);
        Event event1 = createEvent(user, category, location);
        Event event2 = createEvent(user, category, location);
        Comment comment1 = createComment(user, event1, "Event 1 comment");
        Comment comment2 = createComment(user, event2, "Event 2 comment");
        comment2.setStatus(Status.PUBLISHED);

        em.persist(user);
        em.persist(category);
        em.persist(event1);
        em.persist(event2);
        em.persist(comment1);
        em.persist(comment2);
        em.flush();

        MvcResult result = mvc.perform(get(AdminCommentController.URL + "/{eventId}/comments", event2.getId())
                        .param("status", Status.PUBLISHED.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<ResponseCommentDto> response = objectMapper.readValue(content,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ResponseCommentDto.class));

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(event2.getId(), response.getFirst().getEventId());
        assertEquals("Event 2 comment", response.getFirst().getText());
        assertEquals(Status.PUBLISHED, response.getFirst().getStatus());
    }

    @Test
    void update_shouldUpdateComment_whenValidDataProvided() throws Exception {
        User user = createUser("testUser", "test@example.com");
        Category category = createCategory("Test Category");
        Location location = createLocation(55.75f, 37.62f);
        Event event = createEvent(user, category, location);
        Comment comment = createComment(user, event, "Original comment");
        comment.setStatus(Status.PENDING);

        em.persist(user);
        em.persist(category);
        em.persist(event);
        em.persist(comment);
        em.flush();

        UpdateCommentDto updateDto = new UpdateCommentDto(Status.PUBLISHED);

        mvc.perform(patch(AdminCommentController.URL + "/{eventId}/comments/{commentId}", event.getId(), comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNoContent());

        List<Comment> comments = em.createQuery("select c from Comment c where c.id = :id", Comment.class)
                .setParameter("id", comment.getId())
                .getResultList();

        assertEquals(1, comments.size());
        assertEquals(Status.PUBLISHED, comments.getFirst().getStatus());
    }

    @Test
    void update_shouldReturnBadRequest_whenStatusIsNull() throws Exception {
        User user = createUser("testUser", "test@example.com");
        Category category = createCategory("Test Category");
        Location location = createLocation(55.75f, 37.62f);
        Event event = createEvent(user, category, location);
        Comment comment = createComment(user, event, "Original comment");

        em.persist(user);
        em.persist(category);
        em.persist(event);
        em.persist(comment);
        em.flush();

        UpdateCommentDto updateDto = new UpdateCommentDto(null);

        mvc.perform(patch(AdminCommentController.URL + "/{eventId}/comments/{commentId}", event.getId(), comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid argument"))
                .andExpect(jsonPath("$.reason").value("Status can't be null"));
    }

    @Test
    void update_shouldReturnBadRequest_whenStatusIsIncorrect() throws Exception {
        User user = createUser("testUser", "test@example.com");
        Category category = createCategory("Test Category");
        Location location = createLocation(55.75f, 37.62f);
        Event event = createEvent(user, category, location);
        Comment comment = createComment(user, event, "Original comment");

        em.persist(user);
        em.persist(category);
        em.persist(event);
        em.persist(comment);
        em.flush();

        mvc.perform(patch(AdminCommentController.URL + "/{eventId}/comments/{commentId}", event.getId(), comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString("{\"status\": \"some_status\"}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid JSON"));
    }

    @Test
    void update_shouldReturnNotFound_whenCommentDoesNotExist() throws Exception {
        User user = createUser("testUser", "test@example.com");
        Category category = createCategory("Test Category");
        Location location = createLocation(55.75f, 37.62f);
        Event event = createEvent(user, category, location);

        em.persist(user);
        em.persist(category);
        em.persist(event);
        em.flush();

        UpdateCommentDto updateDto = new UpdateCommentDto(Status.PUBLISHED);

        mvc.perform(patch(AdminCommentController.URL + "/{eventId}/comments/{commentId}", event.getId(), 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private Event createEvent(User user, Category category, Location location) {
        return Event.builder()
                .annotation("Test annotation")
                .description("Test description")
                .title("Test event")
                .initiator(user)
                .category(category)
                .location(location)
                .paid(false)
                .participantLimit(0)
                .requestModeration(false)
                .state(State.PUBLISHED)
                .createdOn(LocalDateTime.now())
                .eventDate(LocalDateTime.now().plusDays(1))
                .build();
    }

    private Comment createComment(User user, Event event, String text) {
        return Comment.builder()
                .text(text)
                .event(event)
                .created(LocalDateTime.now())
                .author(user)
                .status(Status.PENDING)
                .build();
    }

    private Category createCategory(String name) {
        return Category.builder()
                .name(name)
                .build();
    }

    private Location createLocation(Float lat, Float lon) {
        return Location.builder()
                .lat(lat)
                .lon(lon)
                .build();
    }

}
