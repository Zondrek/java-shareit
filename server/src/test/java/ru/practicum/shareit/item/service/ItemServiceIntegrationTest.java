package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(User.builder()
                .email("owner@test.com")
                .name("Owner")
                .build());

        booker = userRepository.save(User.builder()
                .email("booker@test.com")
                .name("Booker")
                .build());
    }

    @Test
    void createItem_shouldCreateItemSuccessfully() {
        // Given
        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build();

        // When
        ItemDto created = itemService.createItem(itemDto, owner.getId());

        // Then
        assertThat(created).isNotNull();
        assertThat(created.getId()).isPositive();
        assertThat(created.getName()).isEqualTo("Drill");
        assertThat(created.getDescription()).isEqualTo("Electric drill");
        assertThat(created.getAvailable()).isTrue();
    }

    @Test
    void createItem_shouldThrowNotFoundException_whenUserNotExists() {
        // Given
        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build();

        // When/Then
        assertThatThrownBy(() -> itemService.createItem(itemDto, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    void updateItem_shouldUpdateItemSuccessfully() {
        // Given
        ItemDto createDto = ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build();
        ItemDto created = itemService.createItem(createDto, owner.getId());

        ItemDto updateDto = ItemDto.builder()
                .name("Updated Drill")
                .description("Updated description")
                .available(false)
                .build();

        // When
        ItemDto updated = itemService.updateItem(updateDto, created.getId(), owner.getId());

        // Then
        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getName()).isEqualTo("Updated Drill");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void updateItem_shouldPartiallyUpdate() {
        // Given
        ItemDto createDto = ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build();
        ItemDto created = itemService.createItem(createDto, owner.getId());

        ItemDto updateDto = ItemDto.builder()
                .name("Updated Drill")
                .build();

        // When
        ItemDto updated = itemService.updateItem(updateDto, created.getId(), owner.getId());

        // Then
        assertThat(updated.getName()).isEqualTo("Updated Drill");
        assertThat(updated.getDescription()).isEqualTo("Electric drill"); // Unchanged
        assertThat(updated.getAvailable()).isTrue(); // Unchanged
    }

    @Test
    void updateItem_shouldThrowNotFoundException_whenItemNotExists() {
        // Given
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Drill")
                .build();

        // When/Then
        assertThatThrownBy(() -> itemService.updateItem(updateDto, 999L, owner.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь не найдена");
    }

    @Test
    void updateItem_shouldThrowNotFoundException_whenUserNotOwner() {
        // Given
        ItemDto createDto = ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build();
        ItemDto created = itemService.createItem(createDto, owner.getId());

        ItemDto updateDto = ItemDto.builder()
                .name("Updated Drill")
                .build();

        // When/Then
        assertThatThrownBy(() -> itemService.updateItem(updateDto, created.getId(), booker.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не является владельцем");
    }

    @Test
    void getItem_shouldReturnItemSuccessfully() {
        // Given
        ItemDto createDto = ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build();
        ItemDto created = itemService.createItem(createDto, owner.getId());

        // When
        ItemWithBookingsDto found = itemService.getItem(created.getId(), owner.getId());

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo("Drill");
        assertThat(found.getDescription()).isEqualTo("Electric drill");
        assertThat(found.getAvailable()).isTrue();
    }

    @Test
    void getItem_shouldIncludeBookingsForOwner() {
        // Given
        ItemDto createDto = ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build();
        ItemDto created = itemService.createItem(createDto, owner.getId());

        Item item = itemRepository.findById(created.getId()).orElseThrow();

        // Create past booking
        bookingRepository.save(Booking.builder()
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build());

        // Create future booking
        bookingRepository.save(Booking.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build());

        // When
        ItemWithBookingsDto found = itemService.getItem(created.getId(), owner.getId());

        // Then
        assertThat(found.getLastBooking()).isNotNull();
        assertThat(found.getNextBooking()).isNotNull();
    }

    @Test
    void getItem_shouldThrowNotFoundException_whenItemNotExists() {
        // When/Then
        assertThatThrownBy(() -> itemService.getItem(999L, owner.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь не найдена");
    }

    @Test
    void getItemsByOwner_shouldReturnOwnerItems() {
        // Given
        itemService.createItem(ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build(), owner.getId());

        itemService.createItem(ItemDto.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .build(), owner.getId());

        // When
        Collection<ItemWithBookingsDto> items = itemService.getItemsByOwner(owner.getId());

        // Then
        assertThat(items).hasSize(2);
        assertThat(items).extracting(ItemWithBookingsDto::getName)
                .containsExactlyInAnyOrder("Drill", "Hammer");
    }

    @Test
    void getItemsByOwner_shouldReturnEmptyList_whenNoItems() {
        // When
        Collection<ItemWithBookingsDto> items = itemService.getItemsByOwner(owner.getId());

        // Then
        assertThat(items).isEmpty();
    }

    @Test
    void searchItems_shouldFindItemsByText() {
        // Given
        itemService.createItem(ItemDto.builder()
                .name("Drill")
                .description("Electric drill for home repairs")
                .available(true)
                .build(), owner.getId());

        itemService.createItem(ItemDto.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .build(), owner.getId());

        // When
        Collection<ItemDto> found = itemService.searchItems("drill");

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.iterator().next().getName()).isEqualTo("Drill");
    }

    @Test
    void searchItems_shouldReturnEmptyList_whenTextIsEmpty() {
        // Given
        itemService.createItem(ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build(), owner.getId());

        // When
        Collection<ItemDto> found = itemService.searchItems("");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void addComment_shouldAddCommentSuccessfully() {
        // Given
        ItemDto itemDto = itemService.createItem(ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build(), owner.getId());

        Item item = itemRepository.findById(itemDto.getId()).orElseThrow();

        // Create completed booking
        bookingRepository.save(Booking.builder()
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build());

        CommentDto commentDto = CommentDto.builder()
                .text("Great drill!")
                .build();

        // When
        CommentDto created = itemService.addComment(itemDto.getId(), booker.getId(), commentDto);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.getId()).isPositive();
        assertThat(created.getText()).isEqualTo("Great drill!");
        assertThat(created.getAuthorName()).isEqualTo("Booker");
        assertThat(created.getCreated()).isNotNull();
    }

    @Test
    void addComment_shouldThrowException_whenNoCompletedBooking() {
        // Given
        ItemDto itemDto = itemService.createItem(ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build(), owner.getId());

        CommentDto commentDto = CommentDto.builder()
                .text("Great drill!")
                .build();

        // When/Then
        assertThatThrownBy(() -> itemService.addComment(itemDto.getId(), booker.getId(), commentDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("после завершения аренды");
    }

    @Test
    void addComment_shouldThrowNotFoundException_whenItemNotExists() {
        // Given
        CommentDto commentDto = CommentDto.builder()
                .text("Great!")
                .build();

        // When/Then
        assertThatThrownBy(() -> itemService.addComment(999L, booker.getId(), commentDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь не найдена");
    }

    @Test
    void addComment_shouldThrowNotFoundException_whenUserNotExists() {
        // Given
        ItemDto itemDto = itemService.createItem(ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .build(), owner.getId());

        CommentDto commentDto = CommentDto.builder()
                .text("Great!")
                .build();

        // When/Then
        assertThatThrownBy(() -> itemService.addComment(itemDto.getId(), 999L, commentDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }
}