package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createItem_shouldReturnCreatedItem() throws Exception {
        // Given
        ItemDto requestDto = ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build();

        ItemDto responseDto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build();

        when(itemService.createItem(any(ItemDto.class), eq(1L)))
                .thenReturn(responseDto);

        // When/Then
        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Drill")))
                .andExpect(jsonPath("$.description", is("Electric drill")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    void createItem_shouldReturn400_whenNameIsBlank() throws Exception {
        // Given
        ItemDto requestDto = ItemDto.builder()
                .name("")
                .description("Electric drill")
                .available(true)
                .build();

        // When/Then
        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        // Given
        ItemDto requestDto = ItemDto.builder()
                .name("Updated Drill")
                .build();

        ItemDto responseDto = ItemDto.builder()
                .id(1L)
                .name("Updated Drill")
                .description("Electric drill")
                .available(true)
                .build();

        when(itemService.updateItem(any(ItemDto.class), eq(1L), eq(1L)))
                .thenReturn(responseDto);

        // When/Then
        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated Drill")));
    }

    @Test
    void updateItem_shouldReturn404_whenItemNotExists() throws Exception {
        // Given
        ItemDto requestDto = ItemDto.builder()
                .name("Updated Drill")
                .build();

        when(itemService.updateItem(any(ItemDto.class), eq(999L), eq(1L)))
                .thenThrow(new NotFoundException("Вещь не найдена"));

        // When/Then
        mockMvc.perform(patch("/items/{itemId}", 999L)
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getItem_shouldReturnItem() throws Exception {
        // Given
        ItemWithBookingsDto responseDto = ItemWithBookingsDto.builder()
                .id(1L)
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .comments(List.of())
                .build();

        when(itemService.getItem(1L, 1L))
                .thenReturn(responseDto);

        // When/Then
        mockMvc.perform(get("/items/{itemId}", 1L)
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Drill")));
    }

    @Test
    void getItem_shouldReturn404_whenItemNotExists() throws Exception {
        // Given
        when(itemService.getItem(999L, 1L))
                .thenThrow(new NotFoundException("Вещь не найдена"));

        // When/Then
        mockMvc.perform(get("/items/{itemId}", 999L)
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getItemsByOwner_shouldReturnOwnerItems() throws Exception {
        // Given
        ItemWithBookingsDto item1 = ItemWithBookingsDto.builder()
                .id(1L)
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build();

        ItemWithBookingsDto item2 = ItemWithBookingsDto.builder()
                .id(2L)
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .build();

        when(itemService.getItemsByOwner(1L))
                .thenReturn(List.of(item1, item2));

        // When/Then
        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Drill")))
                .andExpect(jsonPath("$[1].name", is("Hammer")));
    }

    @Test
    void searchItems_shouldReturnMatchingItems() throws Exception {
        // Given
        ItemDto item = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build();

        when(itemService.searchItems("drill"))
                .thenReturn(List.of(item));

        // When/Then
        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Drill")));
    }

    @Test
    void addComment_shouldReturnCreatedComment() throws Exception {
        // Given
        CommentDto requestDto = CommentDto.builder()
                .text("Great drill!")
                .build();

        CommentDto responseDto = CommentDto.builder()
                .id(1L)
                .text("Great drill!")
                .authorName("User")
                .created(LocalDateTime.now())
                .build();

        when(itemService.addComment(eq(1L), eq(1L), any(CommentDto.class)))
                .thenReturn(responseDto);

        // When/Then
        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Great drill!")))
                .andExpect(jsonPath("$.authorName", is("User")));
    }

    @Test
    void addComment_shouldReturn400_whenTextIsBlank() throws Exception {
        // Given
        CommentDto requestDto = CommentDto.builder()
                .text("")
                .build();

        // When/Then
        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_shouldReturn400_whenNoCompletedBooking() throws Exception {
        // Given
        CommentDto requestDto = CommentDto.builder()
                .text("Great!")
                .build();

        when(itemService.addComment(eq(1L), eq(1L), any(CommentDto.class)))
                .thenThrow(new IllegalArgumentException("после завершения аренды"));

        // When/Then
        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }
}