package ru.practicum.shareit.booking.dao;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingShort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select b from Booking as b " +
            "where b.id = ?1 and (b.booker.id = ?2 or b.item.owner.id = ?2)")
    Optional<Booking> findByIdAndUserId(long id, long userId);

    @Query("select b from Booking b " +
            "join fetch b.item it " +
            "join fetch b.booker " +
            "where b.id = ?1 and it.owner.id = ?2")
    Optional<Booking> findByIdAndOwnerId(long id, long ownerId);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"item", "booker"})
    List<Booking> findAllByBookerId(long bookerId, Sort sort);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"item", "booker"})
    List<Booking> findAllByBookerIdAndStatus(long bookerId, BookingStatus status, Sort sort);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"item", "booker"})
    List<Booking> findAllByBookerIdAndStartAfter(long bookerId, LocalDateTime date, Sort sort);

    @Query("select b from Booking as b " +
            "where b.booker.id = ?1 and (?2 between b.start and b.end) " +
            "order by b.start desc")
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"item", "booker"})
    List<Booking> findAllByBookerCurrent(long bookerId, LocalDateTime date);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"item", "booker"})
    List<Booking> findAllByBookerIdAndEndBefore(long bookerId, LocalDateTime date, Sort sort);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"item", "booker"})
    List<Booking> findAllByItemOwnerId(long bookerId, Sort sort);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"item", "booker"})
    List<Booking> findAllByItemOwnerIdAndStatus(long bookerId, BookingStatus status, Sort sort);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"item", "booker"})
    List<Booking> findAllByItemOwnerIdAndStartAfter(long bookerId, LocalDateTime date, Sort sort);

    @Query("select b from Booking as b " +
            "where b.item.owner.id = ?1 and (?2 between b.start and b.end)" +
            "order by b.start desc")
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"item", "booker"})
    List<Booking> findAllByItemOwnerCurrent(long bookerId, LocalDateTime date);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"item", "booker"})
    List<Booking> findAllByItemOwnerIdAndEndBefore(long bookerId, LocalDateTime date, Sort sort);

    @Query("select new ru.practicum.shareit.booking.model.BookingShort(b.id, b.item.id, b.booker.id, b.start, b.end) " +
            "from Booking b " +
            "where b.item.owner.id = ?1 and b.status = 'APPROVED'")
    List<BookingShort> findAllBookingsShortByOwner(long ownerId);

    @Query("select new ru.practicum.shareit.booking.model.BookingShort(b.id, b.item.id, b.booker.id, b.start, b.end) " +
            "from Booking b " +
            "where b.item.id = ?1 and b.status = 'APPROVED'")
    List<BookingShort> findBookingsShortByItem(long itemId);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = {"item", "booker"})
    List<Booking> findByItemIdAndBookerIdAndStatusAndEndBefore(long itemId, long bookerId, BookingStatus status,
                                                         LocalDateTime dateTime);
}
