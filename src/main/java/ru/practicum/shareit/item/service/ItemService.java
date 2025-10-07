package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

import java.util.Collection;

public interface ItemService {

    ItemDto createItem(ItemDto itemDto, long userId);

    ItemDto updateItem(ItemDto itemDto, long itemId, long userId);

    ItemWithBookingsDto getItem(long itemId, long userId);

    Collection<ItemWithBookingsDto> getItemsByOwner(long userId);

    Collection<ItemDto> searchItems(String text);

    CommentDto addComment(long itemId, long userId, CommentDto commentDto);
}