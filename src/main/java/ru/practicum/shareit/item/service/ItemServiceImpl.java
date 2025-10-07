package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto createItem(ItemDto itemDto, long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден, userId = " + userId));
        Item item = ItemMapper.toItem(itemDto, owner);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(ItemDto itemDto, long itemId, long userId) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена, itemId = " + itemId));
        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь не является владельцем данной вещи");
        }
        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }
        Item savedItem = itemRepository.save(existingItem);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemWithBookingsDto getItem(long itemId, long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена, itemId = " + itemId));

        ItemWithBookingsDto dto = mapToItemWithBookingsDto(item);

        // Добавляем информацию о бронированиях только для владельца
        if (item.getOwner().getId().equals(userId)) {
            addBookingInfo(dto, item.getId());
        }

        // Добавляем комментарии
        List<Comment> comments = commentRepository.findByItemId(itemId);
        dto.setComments(CommentMapper.toCommentDtoList(comments));

        return dto;
    }

    @Override
    public Collection<ItemWithBookingsDto> getItemsByOwner(long userId) {
        Collection<Item> items = itemRepository.findByOwnerId(userId);

        if (items.isEmpty()) {
            return List.of();
        }

        // Получаем все ID вещей
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        // Загружаем все бронирования одним запросом
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findByItemIdInAndStatus(itemIds, BookingStatus.APPROVED);

        // Группируем бронирования по itemId
        Map<Long, List<Booking>> bookingsByItem = bookings.stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        return items.stream()
                .map(item -> {
                    ItemWithBookingsDto dto = mapToItemWithBookingsDto(item);

                    // Добавляем информацию о бронированиях из предзагруженных данных
                    List<Booking> itemBookings = bookingsByItem.getOrDefault(item.getId(), List.of());
                    addBookingInfoFromList(dto, itemBookings, now);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> searchItems(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }
        Collection<Item> items = itemRepository.search(text);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(long itemId, long userId, CommentDto commentDto) {
        // Проверка существования пользователя
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден, userId = " + userId));

        // Проверка существования вещи
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена, itemId = " + itemId));

        // Проверка, что пользователь брал вещь в аренду и аренда завершена
        LocalDateTime now = LocalDateTime.now();
        boolean hasCompletedBooking = bookingRepository.existsCompletedBookingByBookerAndItem(
                userId, itemId, now, BookingStatus.APPROVED);

        if (!hasCompletedBooking) {
            throw new IllegalArgumentException("Пользователь может оставить комментарий только после завершения аренды");
        }

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(now)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(savedComment);
    }

    private ItemWithBookingsDto mapToItemWithBookingsDto(Item item) {
        return ItemWithBookingsDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    private void addBookingInfo(ItemWithBookingsDto dto, Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        Booking lastBooking = bookingRepository.findLastBookingForItem(itemId, now, BookingStatus.APPROVED);
        Booking nextBooking = bookingRepository.findNextBookingForItem(itemId, now, BookingStatus.APPROVED);
        setBookingDtoInfo(dto, lastBooking, nextBooking);
    }

    private void addBookingInfoFromList(ItemWithBookingsDto dto, List<Booking> bookings, LocalDateTime now) {
        // Находим последнее завершенное бронирование
        Booking lastBooking = bookings.stream()
                .filter(b -> b.getEnd().isBefore(now))
                .max(Comparator.comparing(Booking::getEnd))
                .orElse(null);

        // Находим ближайшее будущее бронирование
        Booking nextBooking = bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);

        setBookingDtoInfo(dto, lastBooking, nextBooking);
    }

    private void setBookingDtoInfo(ItemWithBookingsDto dto, Booking lastBooking, Booking nextBooking) {
        if (lastBooking != null) {
            dto.setLastBooking(toBookingShortDto(lastBooking));
        }
        if (nextBooking != null) {
            dto.setNextBooking(toBookingShortDto(nextBooking));
        }
    }

    private ItemWithBookingsDto.BookingShortDto toBookingShortDto(Booking booking) {
        return ItemWithBookingsDto.BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .build();
    }
}