package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto createItem(ItemDto itemDto, long userId) {
        User owner = userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден, userId = " + userId));
        Item item = ItemMapper.toItem(itemDto, owner);
        long itemId = itemRepository.createItem(item);
        itemDto.setId(itemId);
        return itemDto;
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, long itemId, long userId) {
        Item existingItem = itemRepository.getItem(itemId)
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
        itemRepository.updateItem(existingItem);
        return ItemMapper.toItemDto(existingItem);
    }

    @Override
    public ItemDto getItem(long itemId) {
        Item item = itemRepository.getItem(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена, itemId = " + itemId));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public Collection<ItemDto> getItemsByOwner(long userId) {
        Collection<Item> items = itemRepository.getItemsByOwner(userId);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> searchItems(String text) {
        Collection<Item> items = itemRepository.searchItems(text);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}