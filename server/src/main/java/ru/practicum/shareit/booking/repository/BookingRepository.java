package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select b " +
            "from Booking b " +
            "join fetch b.booker " +
            "join fetch b.item " +
            "where b.id = :bookingId")
    Optional<Booking> findByIdWithBookerAndItem(@Param("bookingId") Long bookingId);

    List<Booking> findAllByItemIdIn(List<Long> itemIds);

    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    @Query("select b " +
            "from Booking b " +
            "where b.booker.id = :bookerId " +
            "and b.start < :date " +
            "and b.end > :date " +
            "order by b.start desc")
    List<Booking> findAllCurrentBookingsByBookerId(@Param("bookerId") Long bookerId,
                                                   @Param("date") LocalDateTime date);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime date);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime date);

    List<Booking> findAllByItemIdInOrderByStartDesc(Collection<Long> itemIds);

    Optional<Booking> findByItemIdAndBookerId(Long itemId, Long bookerId);

    List<Booking> findAllByItemIdInAndStatusOrderByStartDesc(Collection<Long> itemIds, BookingStatus status);

    @Query("select b " +
            "from Booking b " +
            "where b.item.id in :itemIds " +
            "and b.start < :date " +
            "and b.end > :date " +
            "order by b.start desc")
    List<Booking> findAllCurrentBookingsByItemIds(@Param("itemIds") Collection<Long> itemIds,
                                                  @Param("date") LocalDateTime date);

    List<Booking> findAllByItemIdInAndEndBeforeOrderByStartDesc(Collection<Long> itemIds, LocalDateTime date);

    List<Booking> findAllByItemIdInAndStartAfterOrderByStartDesc(Collection<Long> itemIds, LocalDateTime date);

    Optional<Booking> findFirstByItemIdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime date);

    Optional<Booking> findFirstByItemIdAndEndBeforeOrderByStartDesc(Long itemId, LocalDateTime date);
}
