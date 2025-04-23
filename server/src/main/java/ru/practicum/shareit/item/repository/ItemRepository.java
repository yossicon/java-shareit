package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(Long userId);

    @Query("select i " +
            "from Item i " +
            "join fetch i.owner " +
            "where i.id = :itemId")
    Optional<Item> findByIdWithOwner(@Param("itemId") Long itemId);

    @Query("select i " +
            "from Item i " +
            "where i.available = true " +
            "and lower(i.name) like lower(concat('%', :text, '%')) " +
            "or lower(i.description) like lower(concat('%', :text, '%'))")
    List<Item> searchItem(@Param("text") String text);

    List<Item> findAllByRequestIdIn(List<Long> requestIds);

    List<Item> findAllByRequestId(Long requestId);
}
