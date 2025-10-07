package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.exception.ForbiddenException;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto dto, long bookerId) {
        // Валидация дат
        if (dto.getEnd().isBefore(dto.getStart()) || dto.getEnd().equals(dto.getStart())) {
            throw new IllegalArgumentException("Дата окончания должна быть позже даты начала");
        }

        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден, userId = " + bookerId));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена, itemId = " + dto.getItemId()));

        // Проверка доступности вещи
        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
        }

        // Проверка, что владелец не бронирует свою вещь
        if (item.getOwner().getId().equals(bookerId)) {
            throw new NotFoundException("Владелец не может забронировать свою вещь");
        }

        // Проверка на пересечение с существующими бронированиями
        boolean hasOverlap = bookingRepository.existsOverlappingBooking(
                dto.getItemId(), BookingStatus.REJECTED, dto.getStart(), dto.getEnd());
        if (hasOverlap) {
            throw new IllegalArgumentException("Вещь уже забронирована на указанное время");
        }

        Booking booking = Booking.builder()
                .start(dto.getStart())
                .end(dto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingResponseDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(long bookingId, long ownerId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено, bookingId = " + bookingId));

        // Проверка, что пользователь является владельцем вещи
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Только владелец вещи может подтвердить или отклонить бронирование");
        }

        // Проверка, что бронирование в статусе WAITING
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new IllegalArgumentException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingResponseDto(savedBooking);
    }

    @Override
    public BookingResponseDto getBooking(long bookingId, long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено, bookingId = " + bookingId));

        // Проверка, что пользователь является автором бронирования или владельцем вещи
        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotFoundException("Получить данные о бронировании может только автор бронирования или владелец вещи");
        }

        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getBookingsByBooker(long bookerId, BookingState state) {
        // Проверка существования пользователя
        userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден, userId = " + bookerId));

        LocalDateTime now = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookings = switch (state) {
            case CURRENT -> bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(bookerId, now, now, sort);
            case PAST -> bookingRepository.findByBookerIdAndEndBefore(bookerId, now, sort);
            case FUTURE -> bookingRepository.findByBookerIdAndStartAfter(bookerId, now, sort);
            case WAITING -> bookingRepository.findByBookerIdAndStatus(bookerId, BookingStatus.WAITING, sort);
            case REJECTED -> bookingRepository.findByBookerIdAndStatus(bookerId, BookingStatus.REJECTED, sort);
            default -> bookingRepository.findByBookerId(bookerId, sort);
        };

        return BookingMapper.toBookingResponseDtoList(bookings);
    }

    @Override
    public List<BookingResponseDto> getBookingsByOwner(long ownerId, BookingState state) {
        // Проверка существования пользователя
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден, userId = " + ownerId));

        LocalDateTime now = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookings = switch (state) {
            case CURRENT -> bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(ownerId, now, now, sort);
            case PAST -> bookingRepository.findByItemOwnerIdAndEndBefore(ownerId, now, sort);
            case FUTURE -> bookingRepository.findByItemOwnerIdAndStartAfter(ownerId, now, sort);
            case WAITING -> bookingRepository.findByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING, sort);
            case REJECTED -> bookingRepository.findByItemOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, sort);
            default -> bookingRepository.findByItemOwnerId(ownerId, sort);
        };

        return BookingMapper.toBookingResponseDtoList(bookings);
    }
}