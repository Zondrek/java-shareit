package ru.practicum.shareit.request.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestResponseDto createRequest(Long userId, ItemRequestDto requestDto);

    List<ItemRequestResponseDto> getUserRequests(Long userId);

    List<ItemRequestResponseDto> getAllRequests(Long userId, Pageable pageable);

    ItemRequestResponseDto getRequestById(Long userId, Long requestId);
}