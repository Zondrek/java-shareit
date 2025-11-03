package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {

    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(RestTemplate rest) {
        super(rest);
    }

    public ResponseEntity<Object> createBooking(BookingRequestDto dto, long userId) {
        return post(API_PREFIX, userId, dto);
    }

    public ResponseEntity<Object> approveBooking(long bookingId, long userId, boolean approved) {
        Map<String, Object> parameters = Map.of("approved", approved);
        return patch(API_PREFIX + "/" + bookingId + "?approved={approved}", userId, parameters, null);
    }

    public ResponseEntity<Object> getBooking(long bookingId, long userId) {
        return get(API_PREFIX + "/" + bookingId, userId);
    }

    public ResponseEntity<Object> getBookingsByBooker(long userId, BookingState state) {
        Map<String, Object> parameters = Map.of("state", state.name());
        return get(API_PREFIX + "?state={state}", userId, parameters);
    }

    public ResponseEntity<Object> getBookingsByOwner(long userId, BookingState state) {
        Map<String, Object> parameters = Map.of("state", state.name());
        return get(API_PREFIX + "/owner?state={state}", userId, parameters);
    }
}