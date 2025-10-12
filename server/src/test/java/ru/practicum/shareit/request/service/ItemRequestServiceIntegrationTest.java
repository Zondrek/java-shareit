package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.model.UserResponseDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private UserResponseDto requester;
    private UserResponseDto otherUser;

    @BeforeEach
    void setUp() {
        // Создаём пользователей для тестов
        requester = userService.createUser(UserRequestDto.builder()
                .name("Requester")
                .email("requester@test.com")
                .build());

        otherUser = userService.createUser(UserRequestDto.builder()
                .name("Other User")
                .email("other@test.com")
                .build());
    }

    @Test
    void createRequest_shouldCreateRequestSuccessfully() {
        // Given
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();

        // When
        ItemRequestResponseDto created = itemRequestService.createRequest(requester.getId(), requestDto);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.getId()).isPositive();
        assertThat(created.getDescription()).isEqualTo("Need a drill");
        assertThat(created.getCreated()).isNotNull();
        assertThat(created.getItems()).isEmpty();
    }

    @Test
    void createRequest_shouldThrowNotFoundException_whenUserNotExists() {
        // Given
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();

        // When/Then
        assertThatThrownBy(() -> itemRequestService.createRequest(999L, requestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getUserRequests_shouldReturnUserRequestsWithItems() {
        // Given
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();
        ItemRequestResponseDto request = itemRequestService.createRequest(requester.getId(), requestDto);

        // Создаём item для запроса
        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .requestId(request.getId())
                .build();
        itemService.createItem(itemDto, otherUser.getId());

        // When
        List<ItemRequestResponseDto> requests = itemRequestService.getUserRequests(requester.getId());

        // Then
        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getId()).isEqualTo(request.getId());
        assertThat(requests.get(0).getDescription()).isEqualTo("Need a drill");
        assertThat(requests.get(0).getItems()).hasSize(1);
        assertThat(requests.get(0).getItems().get(0).getName()).isEqualTo("Drill");
    }

    @Test
    void getUserRequests_shouldReturnEmptyList_whenUserHasNoRequests() {
        // When
        List<ItemRequestResponseDto> requests = itemRequestService.getUserRequests(requester.getId());

        // Then
        assertThat(requests).isEmpty();
    }

    @Test
    void getUserRequests_shouldThrowNotFoundException_whenUserNotExists() {
        // When/Then
        assertThatThrownBy(() -> itemRequestService.getUserRequests(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getAllRequests_shouldReturnOtherUsersRequests() {
        // Given
        ItemRequestDto request1 = ItemRequestDto.builder()
                .description("Request 1")
                .build();
        ItemRequestDto request2 = ItemRequestDto.builder()
                .description("Request 2")
                .build();
        ItemRequestDto request3 = ItemRequestDto.builder()
                .description("Request 3")
                .build();

        itemRequestService.createRequest(requester.getId(), request1);
        itemRequestService.createRequest(otherUser.getId(), request2);
        itemRequestService.createRequest(otherUser.getId(), request3);

        // When - получаем запросы от имени requester (не должны видеть свои)
        List<ItemRequestResponseDto> requests = itemRequestService.getAllRequests(
                requester.getId(), PageRequest.of(0, 10));

        // Then
        assertThat(requests).hasSize(2);
        assertThat(requests).extracting(ItemRequestResponseDto::getDescription)
                .containsExactlyInAnyOrder("Request 2", "Request 3");
    }

    @Test
    void getAllRequests_shouldRespectPagination() {
        // Given
        for (int i = 1; i <= 5; i++) {
            itemRequestService.createRequest(otherUser.getId(), ItemRequestDto.builder()
                    .description("Request " + i)
                    .build());
        }

        // When - запрашиваем вторую страницу по 2 элемента
        List<ItemRequestResponseDto> page1 = itemRequestService.getAllRequests(
                requester.getId(), PageRequest.of(0, 2));
        List<ItemRequestResponseDto> page2 = itemRequestService.getAllRequests(
                requester.getId(), PageRequest.of(1, 2));

        // Then
        assertThat(page1).hasSize(2);
        assertThat(page2).hasSize(2);
        // Проверяем, что страницы разные
        assertThat(page1.get(0).getId()).isNotEqualTo(page2.get(0).getId());
    }

    @Test
    void getAllRequests_shouldThrowNotFoundException_whenUserNotExists() {
        // When/Then
        assertThatThrownBy(() -> itemRequestService.getAllRequests(999L, PageRequest.of(0, 10)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getRequestById_shouldReturnRequestWithItems() {
        // Given
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need tools")
                .build();
        ItemRequestResponseDto request = itemRequestService.createRequest(requester.getId(), requestDto);

        // Создаём items для запроса
        ItemDto item1 = ItemDto.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .requestId(request.getId())
                .build();
        ItemDto item2 = ItemDto.builder()
                .name("Screwdriver")
                .description("Phillips screwdriver")
                .available(true)
                .requestId(request.getId())
                .build();
        itemService.createItem(item1, otherUser.getId());
        itemService.createItem(item2, otherUser.getId());

        // When
        ItemRequestResponseDto found = itemRequestService.getRequestById(requester.getId(), request.getId());

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(request.getId());
        assertThat(found.getDescription()).isEqualTo("Need tools");
        assertThat(found.getItems()).hasSize(2);
        assertThat(found.getItems()).extracting(ItemDto::getName)
                .containsExactlyInAnyOrder("Hammer", "Screwdriver");
    }

    @Test
    void getRequestById_shouldThrowNotFoundException_whenUserNotExists() {
        // Given
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();
        ItemRequestResponseDto request = itemRequestService.createRequest(requester.getId(), requestDto);

        // When/Then
        assertThatThrownBy(() -> itemRequestService.getRequestById(999L, request.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getRequestById_shouldThrowNotFoundException_whenRequestNotExists() {
        // When/Then
        assertThatThrownBy(() -> itemRequestService.getRequestById(requester.getId(), 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Item request not found");
    }
}