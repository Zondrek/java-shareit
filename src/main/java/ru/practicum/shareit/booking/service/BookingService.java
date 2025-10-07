package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {

    BookingResponseDto createBooking(BookingRequestDto dto, long bookerId);

    BookingResponseDto approveBooking(long bookingId, long ownerId, boolean approved);

    BookingResponseDto getBooking(long bookingId, long userId);

    List<BookingResponseDto> getBookingsByBooker(long bookerId, BookingState state);

    List<BookingResponseDto> getBookingsByOwner(long ownerId, BookingState state);
}