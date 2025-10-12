package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestResponseDto createRequest(Long userId, ItemRequestDto requestDto) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(requestDto, requester);
        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);

        return ItemRequestMapper.toItemRequestResponseDto(savedRequest, List.of());
    }

    @Override
    public List<ItemRequestResponseDto> getUserRequests(Long userId) {
        // Проверка существования пользователя совмещена с загрузкой запросов
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(userId);

        if (requests.isEmpty()) {
            // Дополнительная проверка существования пользователя только если нет запросов
            if (!userRepository.existsById(userId)) {
                throw new NotFoundException("User not found with id: " + userId);
            }
            return List.of();
        }

        return enrichRequestsWithItems(requests);
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId, Pageable pageable) {
        // Проверка существования пользователя
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found with id: " + userId);
        }

        List<ItemRequest> requests = itemRequestRepository
                .findAllByRequesterIdNotOrderByCreatedDesc(userId, pageable)
                .getContent();

        if (requests.isEmpty()) {
            return List.of();
        }

        return enrichRequestsWithItems(requests);
    }

    @Override
    public ItemRequestResponseDto getRequestById(Long userId, Long requestId) {
        // Проверка существования пользователя
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found with id: " + userId);
        }

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Item request not found with id: " + requestId));

        List<ItemDto> items = itemRepository.findByRequestId(requestId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        return ItemRequestMapper.toItemRequestResponseDto(itemRequest, items);
    }

    private List<ItemRequestResponseDto> enrichRequestsWithItems(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        // Загружаем все items одним запросом и группируем по requestId
        Map<Long, List<ItemDto>> itemsByRequestId = itemRepository.findByRequestIdIn(requestIds).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.groupingBy(ItemDto::getRequestId));

        // Собираем результат
        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestResponseDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), List.of())
                ))
                .collect(Collectors.toList());
    }
}