package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Все бронирования пользователя (как арендатора)
    List<Booking> findByBookerId(Long bookerId, Sort sort);

    // Текущие бронирования пользователя
    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime start, LocalDateTime end, Sort sort);

    // Прошлые бронирования пользователя
    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime end, Sort sort);

    // Будущие бронирования пользователя
    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime start, Sort sort);

    // Бронирования по статусу
    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    // Все бронирования вещей владельца
    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    // Текущие бронирования вещей владельца
    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfter(Long ownerId, LocalDateTime start, LocalDateTime end, Sort sort);

    // Прошлые бронирования вещей владельца
    List<Booking> findByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime now, Sort sort);

    // Будущие бронирования вещей владельца
    List<Booking> findByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime now, Sort sort);

    // Бронирования вещей владельца по статусу
    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    // Последнее завершенное бронирование для вещи
    @Query("select b from Booking b " +
            "where b.item.id = ?1 " +
            "and b.end < ?2 " +
            "and b.status = ?3 " +
            "order by b.end desc " +
            "limit 1")
    Booking findLastBookingForItem(Long itemId, LocalDateTime now, BookingStatus status);

    // Ближайшее будущее бронирование для вещи
    @Query("select b from Booking b " +
            "where b.item.id = ?1 " +
            "and b.start > ?2 " +
            "and b.status = ?3 " +
            "order by b.start asc " +
            "limit 1")
    Booking findNextBookingForItem(Long itemId, LocalDateTime now, BookingStatus status);

    // Проверка, брал ли пользователь вещь в аренду
    @Query("select count(b) > 0 from Booking b " +
            "where b.booker.id = ?1 " +
            "and b.item.id = ?2 " +
            "and b.end < ?3 " +
            "and b.status = ?4")
    boolean existsCompletedBookingByBookerAndItem(Long bookerId, Long itemId, LocalDateTime now, BookingStatus status);

    // Получить все бронирования для списка вещей
    @Query("select b from Booking b " +
            "where b.item.id in ?1 " +
            "and b.status = ?2 " +
            "order by b.start asc")
    List<Booking> findByItemIdInAndStatus(List<Long> itemIds, BookingStatus status);

    // Проверка пересечения бронирований
    @Query("select count(b) > 0 from Booking b " +
            "where b.item.id = ?1 " +
            "and b.status != ?2 " +
            "and ((b.start < ?4 and b.end > ?3) " +
            "or (b.start >= ?3 and b.start < ?4))")
    boolean existsOverlappingBooking(Long itemId, BookingStatus rejectedStatus, LocalDateTime start, LocalDateTime end);
}