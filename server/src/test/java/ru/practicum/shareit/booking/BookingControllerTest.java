package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.error.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createBooking_shouldReturnCreatedBooking() throws Exception {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(1L)
                .start(requestDto.getStart())
                .end(requestDto.getEnd())
                .status(BookingStatus.WAITING)
                .item(BookingResponseDto.ItemDto.builder().id(1L).name("Drill").build())
                .booker(BookingResponseDto.BookerDto.builder().id(1L).name("Booker").build())
                .build();

        when(bookingService.createBooking(any(BookingRequestDto.class), eq(1L)))
                .thenReturn(responseDto);

        // When/Then
        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("WAITING")));
    }

    @Test
    void approveBooking_shouldReturnApprovedBooking() throws Exception {
        // Given
        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.APPROVED)
                .item(BookingResponseDto.ItemDto.builder().id(1L).name("Drill").build())
                .booker(BookingResponseDto.BookerDto.builder().id(1L).name("Booker").build())
                .build();

        when(bookingService.approveBooking(1L, 1L, true))
                .thenReturn(responseDto);

        // When/Then
        mockMvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void getBooking_shouldReturnBooking() throws Exception {
        // Given
        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .item(BookingResponseDto.ItemDto.builder().id(1L).name("Drill").build())
                .booker(BookingResponseDto.BookerDto.builder().id(1L).name("Booker").build())
                .build();

        when(bookingService.getBooking(1L, 1L))
                .thenReturn(responseDto);

        // When/Then
        mockMvc.perform(get("/bookings/{bookingId}", 1L)
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void getBooking_shouldReturn404_whenBookingNotExists() throws Exception {
        // Given
        when(bookingService.getBooking(999L, 1L))
                .thenThrow(new NotFoundException("Бронирование не найдено"));

        // When/Then
        mockMvc.perform(get("/bookings/{bookingId}", 999L)
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingsByBooker_shouldReturnBookerBookings() throws Exception {
        // Given
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .item(BookingResponseDto.ItemDto.builder().id(1L).name("Drill").build())
                .booker(BookingResponseDto.BookerDto.builder().id(1L).name("Booker").build())
                .build();

        when(bookingService.getBookingsByBooker(1L, BookingState.ALL))
                .thenReturn(List.of(booking));

        // When/Then
        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void getBookingsByOwner_shouldReturnOwnerBookings() throws Exception {
        // Given
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .item(BookingResponseDto.ItemDto.builder().id(1L).name("Drill").build())
                .booker(BookingResponseDto.BookerDto.builder().id(1L).name("Booker").build())
                .build();

        when(bookingService.getBookingsByOwner(1L, BookingState.ALL))
                .thenReturn(List.of(booking));

        // When/Then
        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }
}