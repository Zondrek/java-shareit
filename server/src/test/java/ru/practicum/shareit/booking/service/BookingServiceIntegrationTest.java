package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.error.exception.ForbiddenException;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.model.UserResponseDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private UserResponseDto owner;
    private UserResponseDto booker;
    private ItemDto item;

    @BeforeEach
    void setUp() {
        owner = userService.createUser(UserRequestDto.builder()
                .email("owner@test.com")
                .name("Owner")
                .build());

        booker = userService.createUser(UserRequestDto.builder()
                .email("booker@test.com")
                .name("Booker")
                .build());

        item = itemService.createItem(ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build(), owner.getId());
    }

    @Test
    void createBooking_shouldCreateBookingSuccessfully() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        // When
        BookingResponseDto created = bookingService.createBooking(requestDto, booker.getId());

        // Then
        assertThat(created).isNotNull();
        assertThat(created.getId()).isPositive();
        assertThat(created.getItem().getId()).isEqualTo(item.getId());
        assertThat(created.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(created.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void createBooking_shouldThrowNotFoundException_whenItemNotExists() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(999L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking(requestDto, booker.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void approveBooking_shouldApproveSuccessfully() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingResponseDto booking = bookingService.createBooking(requestDto, booker.getId());

        // When
        BookingResponseDto approved = bookingService.approveBooking(booking.getId(), owner.getId(), true);

        // Then
        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approveBooking_shouldRejectSuccessfully() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingResponseDto booking = bookingService.createBooking(requestDto, booker.getId());

        // When
        BookingResponseDto rejected = bookingService.approveBooking(booking.getId(), owner.getId(), false);

        // Then
        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void getBooking_shouldReturnBooking() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingResponseDto created = bookingService.createBooking(requestDto, booker.getId());

        // When
        BookingResponseDto found = bookingService.getBooking(created.getId(), booker.getId());

        // Then
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    void getBooking_shouldThrowNotFoundException_whenBookingNotExists() {
        // When/Then
        assertThatThrownBy(() -> bookingService.getBooking(999L, booker.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getBookingsByBooker_shouldReturnBookerBookings() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        bookingService.createBooking(requestDto, booker.getId());

        // When
        List<BookingResponseDto> bookings = bookingService.getBookingsByBooker(booker.getId(), BookingState.ALL);

        // Then
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void getBookingsByOwner_shouldReturnOwnerBookings() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        bookingService.createBooking(requestDto, booker.getId());

        // When
        List<BookingResponseDto> bookings = bookingService.getBookingsByOwner(owner.getId(), BookingState.ALL);

        // Then
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    void createBooking_shouldThrowException_whenEndBeforeStart() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking(requestDto, booker.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("позже даты начала");
    }

    @Test
    void createBooking_shouldThrowException_whenEndEqualsStart() {
        // Given
        LocalDateTime now = LocalDateTime.now().plusDays(1);
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(now)
                .end(now)
                .build();

        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking(requestDto, booker.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("позже даты начала");
    }

    @Test
    void createBooking_shouldThrowException_whenItemNotAvailable() {
        // Given
        ItemDto unavailableItem = itemService.createItem(ItemDto.builder()
                .name("Unavailable Drill")
                .description("Not available")
                .available(false)
                .build(), owner.getId());

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(unavailableItem.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking(requestDto, booker.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("недоступна");
    }

    @Test
    void createBooking_shouldThrowException_whenOwnerTriesToBookOwnItem() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking(requestDto, owner.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Владелец не может забронировать свою вещь");
    }

    @Test
    void approveBooking_shouldThrowException_whenNotOwner() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingResponseDto booking = bookingService.createBooking(requestDto, booker.getId());

        UserResponseDto otherUser = userService.createUser(UserRequestDto.builder()
                .email("other@test.com")
                .name("Other")
                .build());

        // When/Then
        assertThatThrownBy(() -> bookingService.approveBooking(booking.getId(), otherUser.getId(), true))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Только владелец");
    }

    @Test
    void approveBooking_shouldThrowException_whenAlreadyApproved() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingResponseDto booking = bookingService.createBooking(requestDto, booker.getId());
        bookingService.approveBooking(booking.getId(), owner.getId(), true);

        // When/Then
        assertThatThrownBy(() -> bookingService.approveBooking(booking.getId(), owner.getId(), true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("уже обработано");
    }

    @Test
    void getBooking_shouldThrowException_whenNotBookerOrOwner() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingResponseDto booking = bookingService.createBooking(requestDto, booker.getId());

        UserResponseDto otherUser = userService.createUser(UserRequestDto.builder()
                .email("other@test.com")
                .name("Other")
                .build());

        // When/Then
        assertThatThrownBy(() -> bookingService.getBooking(booking.getId(), otherUser.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("только автор бронирования или владелец");
    }

    @Test
    void getBookingsByBooker_shouldReturnCurrentBookings() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(1))
                .build();
        bookingService.createBooking(requestDto, booker.getId());

        // When
        List<BookingResponseDto> bookings = bookingService.getBookingsByBooker(booker.getId(), BookingState.CURRENT);

        // Then
        assertThat(bookings).hasSize(1);
    }

    @Test
    void getBookingsByBooker_shouldReturnPastBookings() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .build();
        bookingService.createBooking(requestDto, booker.getId());

        // When
        List<BookingResponseDto> bookings = bookingService.getBookingsByBooker(booker.getId(), BookingState.PAST);

        // Then
        assertThat(bookings).hasSize(1);
    }

    @Test
    void getBookingsByBooker_shouldReturnFutureBookings() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        bookingService.createBooking(requestDto, booker.getId());

        // When
        List<BookingResponseDto> bookings = bookingService.getBookingsByBooker(booker.getId(), BookingState.FUTURE);

        // Then
        assertThat(bookings).hasSize(1);
    }

    @Test
    void getBookingsByBooker_shouldReturnWaitingBookings() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        bookingService.createBooking(requestDto, booker.getId());

        // When
        List<BookingResponseDto> bookings = bookingService.getBookingsByBooker(booker.getId(), BookingState.WAITING);

        // Then
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void getBookingsByBooker_shouldReturnRejectedBookings() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingResponseDto booking = bookingService.createBooking(requestDto, booker.getId());
        bookingService.approveBooking(booking.getId(), owner.getId(), false);

        // When
        List<BookingResponseDto> bookings = bookingService.getBookingsByBooker(booker.getId(), BookingState.REJECTED);

        // Then
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void getBookingsByOwner_shouldReturnCurrentBookings() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(1))
                .build();
        bookingService.createBooking(requestDto, booker.getId());

        // When
        List<BookingResponseDto> bookings = bookingService.getBookingsByOwner(owner.getId(), BookingState.CURRENT);

        // Then
        assertThat(bookings).hasSize(1);
    }

    @Test
    void getBookingsByOwner_shouldReturnPastBookings() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .build();
        bookingService.createBooking(requestDto, booker.getId());

        // When
        List<BookingResponseDto> bookings = bookingService.getBookingsByOwner(owner.getId(), BookingState.PAST);

        // Then
        assertThat(bookings).hasSize(1);
    }

    @Test
    void getBookingsByOwner_shouldReturnFutureBookings() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        bookingService.createBooking(requestDto, booker.getId());

        // When
        List<BookingResponseDto> bookings = bookingService.getBookingsByOwner(owner.getId(), BookingState.FUTURE);

        // Then
        assertThat(bookings).hasSize(1);
    }

    @Test
    void getBookingsByOwner_shouldReturnWaitingBookings() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        bookingService.createBooking(requestDto, booker.getId());

        // When
        List<BookingResponseDto> bookings = bookingService.getBookingsByOwner(owner.getId(), BookingState.WAITING);

        // Then
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void getBookingsByOwner_shouldReturnRejectedBookings() {
        // Given
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        BookingResponseDto booking = bookingService.createBooking(requestDto, booker.getId());
        bookingService.approveBooking(booking.getId(), owner.getId(), false);

        // When
        List<BookingResponseDto> bookings = bookingService.getBookingsByOwner(owner.getId(), BookingState.REJECTED);

        // Then
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.REJECTED);
    }
}