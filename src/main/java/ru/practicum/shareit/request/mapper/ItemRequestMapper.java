package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(ItemRequestDto dto, User requester) {
        return ItemRequest.builder()
                .description(dto.getDescription())
                .requester(requester)
                .created(LocalDateTime.now())
                .build();
    }

    public static ItemRequestResponseDto toItemRequestResponseDto(ItemRequest itemRequest, List<ItemDto> items) {
        return ItemRequestResponseDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .items(items)
                .build();
    }
}