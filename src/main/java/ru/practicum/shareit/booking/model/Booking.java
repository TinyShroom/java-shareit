package ru.practicum.shareit.booking.model;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "booking")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "item_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Item item;

    @JoinColumn(name = "booker_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User booker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;
    @Column(name = "start_booking", nullable = false)
    private LocalDateTime start;
    @Column(name = "end_booking", nullable = false)
    private LocalDateTime end;

}
