package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemRepository {

    long createItem(Item item);

    void updateItem(Item item);

    Optional<Item> getItem(long itemId);

    Collection<Item> getItemsByOwner(long ownerId);

    Collection<Item> searchItems(String text);
}