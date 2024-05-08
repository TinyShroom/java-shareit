package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

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

    @Query("select b from Booking b " +
            "join fetch b.item " +
            "join fetch b.booker " +
            "where b.booker.id = ?1 " +
            "order by b.start desc")
    List<Booking> findAllByBooker(long bookerId);

    @Query("select b from Booking b " +
            "join fetch b.item " +
            "join fetch b.booker " +
            "where b.booker.id = ?1 and b.status = ?2 " +
            "order by b.start desc")
    List<Booking> findAllByBookerAndStatus(long bookerId, BookingStatus status);

    @Query("select b from Booking b " +
            "join fetch b.item " +
            "join fetch b.booker " +
            "where b.booker.id = ?1 and b.start > ?2 " +
            "order by b.start desc")
    List<Booking> findAllByBookerFuture(long bookerId, LocalDateTime date);

    @Query("select b from Booking as b " +
            "join fetch b.item " +
            "join fetch b.booker " +
            "where b.booker.id = ?1 and (?2 between b.start and b.end) " +
            "order by b.start desc")
    List<Booking> findAllByBookerCurrent(long bookerId, LocalDateTime date);

    @Query("select b from Booking b " +
            "join fetch b.item " +
            "join fetch b.booker " +
            "where b.booker.id = ?1 and b.end < ?2 " +
            "order by b.start desc")
    List<Booking> findAllByBookerPast(long bookerId, LocalDateTime date);

    @Query("select b from Booking b " +
            "join fetch b.item " +
            "join fetch b.booker " +
            "where b.item.owner.id = ?1 " +
            "order by b.start desc")
    List<Booking> findAllByOwner(long bookerId);

    @Query("select b from Booking b " +
            "join fetch b.item " +
            "join fetch b.booker " +
            "where b.item.owner.id = ?1 and b.status = ?2 " +
            "order by b.start desc")
    List<Booking> findAllByOwnerAndStatus(long bookerId, BookingStatus status);

    @Query("select b from Booking b " +
            "join fetch b.item " +
            "join fetch b.booker " +
            "where b.item.owner.id = ?1 and b.start > ?2 " +
            "order by b.start desc")
    List<Booking> findAllByOwnerFuture(long bookerId, LocalDateTime date);

    @Query("select b from Booking as b " +
            "join fetch b.item " +
            "join fetch b.booker " +
            "where b.item.owner.id = ?1 and (?2 between b.start and b.end) " +
            "order by b.start desc")
    List<Booking> findAllByOwnerCurrent(long bookerId, LocalDateTime date);

    @Query("select b from Booking b " +
            "join fetch b.item " +
            "join fetch b.booker " +
            "where b.item.owner.id = ?1 and b.end < ?2 " +
            "order by b.start desc")
    List<Booking> findAllByOwnerPast(long bookerId, LocalDateTime date);

    @Query("select new ru.practicum.shareit.booking.dto.BookingDtoShort(b.id, b.item.id, b.booker.id, b.start, b.end) " +
            "from Booking b " +
            "where b.item.owner.id = ?1 and b.status = 'APPROVED'")
    List<BookingDtoShort> findAllBookingsShortByOwner(long ownerId);

    @Query("select new ru.practicum.shareit.booking.dto.BookingDtoShort(b.id, b.item.id, b.booker.id, b.start, b.end) " +
            "from Booking b " +
            "where b.item.id = ?1 and b.status = 'APPROVED'")
    List<BookingDtoShort> findBookingsShortByItem(long itemId);

    List<Booking> findByItemIdAndBookerIdAndStatusAndEndBefore(long itemId, long bookerId, BookingStatus status,
                                                         LocalDateTime dateTime);
}
