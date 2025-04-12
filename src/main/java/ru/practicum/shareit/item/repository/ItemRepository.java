package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository {

    long createItem(Item item);

    void updateItem(Item item);

    Item getItem(long itemId);

    Collection<Item> getItemsByOwner(long ownerId);

    Collection<Item> searchItems(String text);
}