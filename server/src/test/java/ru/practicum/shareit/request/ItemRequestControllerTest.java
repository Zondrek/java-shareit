package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createRequest_shouldReturnCreatedRequest() throws Exception {
        // Given
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();

        ItemRequestResponseDto responseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .items(List.of())
                .build();

        when(itemRequestService.createRequest(eq(1L), any(ItemRequestDto.class)))
                .thenReturn(responseDto);

        // When/Then
        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Need a drill")))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void createRequest_shouldReturn404_whenUserNotExists() throws Exception {
        // Given
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();

        when(itemRequestService.createRequest(eq(999L), any(ItemRequestDto.class)))
                .thenThrow(new NotFoundException("User not found"));

        // When/Then
        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserRequests_shouldReturnUserRequests() throws Exception {
        // Given
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .requestId(1L)
                .build();

        ItemRequestResponseDto responseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .items(List.of(itemDto))
                .build();

        when(itemRequestService.getUserRequests(1L))
                .thenReturn(List.of(responseDto));

        // When/Then
        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("Need a drill")))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].name", is("Drill")));
    }

    @Test
    void getUserRequests_shouldReturnEmptyList_whenNoRequests() throws Exception {
        // Given
        when(itemRequestService.getUserRequests(1L))
                .thenReturn(List.of());

        // When/Then
        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllRequests_shouldReturnOtherUsersRequests() throws Exception {
        // Given
        ItemRequestResponseDto request1 = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Request 1")
                .created(LocalDateTime.now())
                .items(List.of())
                .build();

        ItemRequestResponseDto request2 = ItemRequestResponseDto.builder()
                .id(2L)
                .description("Request 2")
                .created(LocalDateTime.now())
                .items(List.of())
                .build();

        when(itemRequestService.getAllRequests(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of(request1, request2));

        // When/Then
        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    void getAllRequests_shouldUseDefaultPaginationParams() throws Exception {
        // Given
        when(itemRequestService.getAllRequests(eq(1L), eq(PageRequest.of(0, 10))))
                .thenReturn(List.of());

        // When/Then
        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getRequestById_shouldReturnRequest() throws Exception {
        // Given
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .requestId(1L)
                .build();

        ItemRequestResponseDto responseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .items(List.of(itemDto))
                .build();

        when(itemRequestService.getRequestById(1L, 1L))
                .thenReturn(responseDto);

        // When/Then
        mockMvc.perform(get("/requests/{requestId}", 1L)
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Need a drill")))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].name", is("Drill")));
    }

    @Test
    void getRequestById_shouldReturn404_whenRequestNotExists() throws Exception {
        // Given
        when(itemRequestService.getRequestById(1L, 999L))
                .thenThrow(new NotFoundException("Item request not found"));

        // When/Then
        mockMvc.perform(get("/requests/{requestId}", 999L)
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isNotFound());
    }
}