package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, Status status);

    @Query("select b " +
            "from Booking as b " +
            "where b.booker.id = ?1 " +
            "AND b.start < ?2 " +
            "AND b.end > ?2 " +
            "order by b.start desc")
    List<Booking> findAllCurrentBookingsByBookerId(Long bookerId, LocalDateTime date);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime date);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime date);

    List<Booking> findAllByItemIdInOrderByStartDesc(Collection<Long> itemIds);

    Optional<Booking> findByItemIdAndBookerId(Long itemId, Long bookerId);

    List<Booking> findAllByItemIdInAndStatusOrderByStartDesc(Collection<Long> itemIds, Status status);

    @Query("select b " +
            "from Booking as b " +
            "where b.item.id in ?1 " +
            "AND b.start < ?2 " +
            "AND b.end > ?2 " +
            "order by b.start desc")
    List<Booking> findAllCurrentBookingsByItemIds(Collection<Long> itemIds, LocalDateTime date);

    List<Booking> findAllByItemIdInAndEndBeforeOrderByStartDesc(Collection<Long> itemIds, LocalDateTime date);

    List<Booking> findAllByItemIdInAndStartAfterOrderByStartDesc(Collection<Long> itemIds, LocalDateTime date);

    Optional<Booking> findFirstByItemIdAndStartAfterOrderByStartDesc(Long itemId, LocalDateTime date);

    Optional<Booking> findFirstByItemIdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime date);
}
