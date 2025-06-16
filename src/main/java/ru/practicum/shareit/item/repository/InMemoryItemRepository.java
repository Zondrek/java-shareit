package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();
    private final Map<Long, Set<Item>> ownerItems = new HashMap<>();

    @Override
    public long createItem(Item item) {
        item.setId(createId());
        items.put(item.getId(), item);
        updateOwner(item);
        return item.getId();
    }

    @Override
    public void updateItem(Item item) {
        items.put(item.getId(), item);
        updateOwner(item);
    }

    @Override
    public Optional<Item> getItem(long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public Collection<Item> getItemsByOwner(long ownerId) {
        if (ownerItems.containsKey(ownerId)) {
            return ownerItems.get(ownerId);
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<Item> searchItems(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String lowerText = text.toLowerCase();
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(lowerText) ||
                        item.getDescription().toLowerCase().contains(lowerText))
                .collect(Collectors.toList());
    }

    private void updateOwner(Item item) {
        Set<Item> items = ownerItems.computeIfAbsent(item.getOwner().getId(), k -> new HashSet<>());
        items.add(item);
    }

    private long createId() {
        long currentMaxId = items.values()
                .stream()
                .map(Item::getId)
                .max(Long::compareTo)
                .orElse(0L);
        return ++currentMaxId;
    }
}