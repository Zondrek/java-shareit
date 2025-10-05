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
    @Query("select b from Booking b " +
            "where b.booker.id = ?1 " +
            "and b.start < ?2 and b.end > ?2")
    List<Booking> findCurrentByBookerId(Long bookerId, LocalDateTime now, Sort sort);

    // Прошлые бронирования пользователя
    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Sort sort);

    // Будущие бронирования пользователя
    List<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime start, Sort sort);

    // Бронирования по статусу
    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    // Все бронирования вещей владельца
    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1")
    List<Booking> findByOwnerId(Long ownerId, Sort sort);

    // Текущие бронирования вещей владельца
    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.start < ?2 and b.end > ?2")
    List<Booking> findCurrentByOwnerId(Long ownerId, LocalDateTime now, Sort sort);

    // Прошлые бронирования вещей владельца
    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.end < ?2")
    List<Booking> findPastByOwnerId(Long ownerId, LocalDateTime now, Sort sort);

    // Будущие бронирования вещей владельца
    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.start > ?2")
    List<Booking> findFutureByOwnerId(Long ownerId, LocalDateTime now, Sort sort);

    // Бронирования вещей владельца по статусу
    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.status = ?2")
    List<Booking> findByOwnerIdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    // Бронирования для конкретной вещи
    List<Booking> findByItemId(Long itemId, Sort sort);

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
}