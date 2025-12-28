package ru.practicum.explorewithme.comment.controller;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.explorewithme.api.event.dto.EventFullDto;
import ru.practicum.explorewithme.comment.client.event.EventClient;
import ru.practicum.explorewithme.comment.dto.ResponseCommentDto;
import ru.practicum.explorewithme.comment.dto.UpdateCommentDto;
import ru.practicum.explorewithme.comment.enums.Status;
import ru.practicum.explorewithme.comment.model.Comment;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.config.location=classpath:application-test.yml"
})
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class AdminCommentControllerTest {

    private final ObjectMapper objectMapper;

    private final EntityManager em;

    private MockMvc mvc;

    @MockBean
    private EventClient eventClient;

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        em.clear();
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void getAll_shouldReturnComments_whenNoStatusProvided() throws Exception {
        Comment comment1 = Comment.builder()
                .text("First comment")
                .eventId(1L)
                .authorId(1L)
                .status(Status.PENDING)
                .created(LocalDateTime.now().minusDays(1))
                .build();
        Comment comment2 = Comment.builder()
                .text("Second comment")
                .eventId(2L)
                .authorId(2L)
                .status(Status.PUBLISHED)
                .created(LocalDateTime.now())
                .build();

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
        Comment comment1 = Comment.builder()
                .text("First comment")
                .eventId(1L)
                .authorId(1L)
                .status(Status.PUBLISHED)
                .created(LocalDateTime.now().minusDays(1))
                .build();
        Comment comment2 = Comment.builder()
                .text("Second comment")
                .eventId(2L)
                .authorId(2L)
                .status(Status.PENDING)
                .created(LocalDateTime.now())
                .build();

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
        Comment comment1 = Comment.builder()
                .text("First comment")
                .eventId(1L)
                .authorId(1L)
                .status(Status.PUBLISHED)
                .created(LocalDateTime.now().minusMinutes(1))
                .build();
        Comment comment2 = Comment.builder()
                .text("Second comment")
                .eventId(1L)
                .authorId(1L)
                .status(Status.PENDING)
                .created(LocalDateTime.now().minusMinutes(2))
                .build();
        Comment comment3 = Comment.builder()
                .text("3 comment")
                .eventId(1L)
                .authorId(1L)
                .status(Status.PENDING)
                .created(LocalDateTime.now().minusMinutes(3))
                .build();
        Comment comment4 = Comment.builder()
                .text("4 comment")
                .eventId(1L)
                .authorId(1L)
                .status(Status.PENDING)
                .created(LocalDateTime.now().minusMinutes(4))
                .build();
        Comment comment5 = Comment.builder()
                .text("5 comment")
                .eventId(1L)
                .authorId(1L)
                .status(Status.PENDING)
                .created(LocalDateTime.now().minusMinutes(5))
                .build();

        em.persist(comment1);
        em.persist(comment2);
        em.persist(comment3);
        em.persist(comment4);
        em.persist(comment5);
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
        assertEquals("3 comment", response.getFirst().getText());
        assertEquals("4 comment", response.getLast().getText());
    }

    @Test
    void getByEventId_shouldReturnCommentsForEvent() throws Exception {
        Comment comment1 = Comment.builder()
                .text("Event 1 comment")
                .eventId(1L)
                .authorId(1L)
                .status(Status.PENDING)
                .created(LocalDateTime.now().minusDays(1))
                .build();
        Comment comment2 = Comment.builder()
                .text("Event 2 comment")
                .eventId(2L)
                .authorId(2L)
                .status(Status.PENDING)
                .created(LocalDateTime.now())
                .build();

        em.persist(comment1);
        em.persist(comment2);
        em.flush();

        when(eventClient.getByIdAndState(1L, null))
                .thenReturn(EventFullDto.builder().id(1L).title("Test Event 1").build());

        MvcResult result = mvc.perform(get(AdminCommentController.URL + "/{eventId}/comments", 1L)
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
        assertEquals(1L, response.getFirst().getEventId());
    }

    @Test
    void getByEventId_shouldReturnCommentsForEventFilteredByStatus() throws Exception {
        Comment comment1 = Comment.builder()
                .text("Event 1 comment")
                .eventId(1L)
                .authorId(1L)
                .status(Status.PENDING)
                .created(LocalDateTime.now().minusDays(1))
                .build();
        Comment comment2 = Comment.builder()
                .text("Event 2 comment")
                .eventId(2L)
                .authorId(2L)
                .status(Status.PUBLISHED)
                .created(LocalDateTime.now())
                .build();

        em.persist(comment1);
        em.persist(comment2);
        em.flush();

        when(eventClient.getByIdAndState(2L, null))
                .thenReturn(EventFullDto.builder().id(2L).title("Test Event 2").build());

        MvcResult result = mvc.perform(get(AdminCommentController.URL + "/{eventId}/comments", 2L)
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
        assertEquals(2L, response.getFirst().getEventId());
        assertEquals("Event 2 comment", response.getFirst().getText());
        assertEquals(Status.PUBLISHED, response.getFirst().getStatus());
    }

    @Test
    void update_shouldUpdateComment_whenValidDataProvided() throws Exception {
        Comment comment = Comment.builder()
                .text("Original comment")
                .eventId(1L)
                .authorId(1L)
                .status(Status.PENDING)
                .created(LocalDateTime.now())
                .build();

        em.persist(comment);
        em.flush();

        UpdateCommentDto updateDto = new UpdateCommentDto(Status.PUBLISHED);

        mvc.perform(patch(AdminCommentController.URL + "/{eventId}/comments/{commentId}", 1L, comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNoContent());

        List<Comment> comments = em.createQuery("select c from Comment c where c.id = :id", Comment.class)
                .setParameter("id", comment.getId())
                .getResultList();

        assertEquals(1, comments.size());
        assertEquals(Status.PUBLISHED, comments.getFirst().getStatus());
        assertEquals(comment.getText(), comments.getFirst().getText());
    }

    @Test
    void update_shouldReturnBadRequest_whenStatusIsNull() throws Exception {
        Comment comment = Comment.builder()
                .text("Original comment")
                .eventId(1L)
                .authorId(1L)
                .status(Status.PENDING)
                .created(LocalDateTime.now())
                .build();

        em.persist(comment);
        em.flush();

        UpdateCommentDto updateDto = new UpdateCommentDto(null);

        mvc.perform(patch(AdminCommentController.URL + "/{eventId}/comments/{commentId}", 1L, comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid argument"))
                .andExpect(jsonPath("$.reason").value("Status can't be null"));
    }

    @Test
    void update_shouldReturnBadRequest_whenStatusIsIncorrect() throws Exception {
        Comment comment = Comment.builder()
                .text("Original comment")
                .eventId(1L)
                .authorId(1L)
                .status(Status.PENDING)
                .created(LocalDateTime.now())
                .build();

        em.persist(comment);
        em.flush();

        mvc.perform(patch(AdminCommentController.URL + "/{eventId}/comments/{commentId}", 1L, comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString("{\"status\": \"some_status\"}")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid JSON"));
    }

    @Test
    void update_shouldReturnNotFound_whenCommentDoesNotExist() throws Exception {
        UpdateCommentDto updateDto = new UpdateCommentDto(Status.PUBLISHED);

        mvc.perform(patch(AdminCommentController.URL + "/{eventId}/comments/{commentId}", 1L, 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

}
