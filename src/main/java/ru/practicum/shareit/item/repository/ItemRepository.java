package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(Long userId);

    @Query("select i " +
            "from Item i " +
            "join fetch i.owner " +
            "where i.id = ?1")
    Optional<Item> findByIdWithOwner(Long itemId);

    @Query("select i " +
            "from Item i " +
            "where i.available = true " +
            "and lower(i.name) like lower(concat('%', ?1, '%')) " +
            "or lower(i.description) like lower(concat('%', ?1, '%'))")
    List<Item> searchItem(String text);
}
