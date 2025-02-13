package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
public class ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private final Map<Long, Set<Long>> userItems = new HashMap<>();

    public Item addItem(Long userId, Item item) {
        item.setId(getNextId());
        item.setUserId(userId);
        items.put(item.getId(), item);
        userItems.putIfAbsent(userId, new HashSet<>());
        Set<Long> itemsSet = userItems.get(userId);
        if (itemsSet.contains(item.getId())) {
            throw new DuplicatedDataException(String.format("Вещь %s уже добавлена пользователем с id %d",
                    item.getName(), userId));
        }
        itemsSet.add(item.getId());
        return item;
    }

    public List<Item> getAllUserItems(Long userId) {
        return userItems.get(userId).stream()
                .map(items::get)
                .toList();
    }

    public Optional<Item> getItemById(Long itemId) {
        return Optional.of(items.get(itemId));
    }

    public List<Item> searchItem(String text) {
        return items.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                .filter(Item::getAvailable)
                .toList();
    }

    public Item updateItem(Long itemId, Item newItem) {
        items.put(itemId, newItem);
        return newItem;
    }

    private long getNextId() {
        long maxId = items.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++maxId;
    }
}
