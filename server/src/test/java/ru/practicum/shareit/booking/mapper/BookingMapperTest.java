package ru.practicum.shareit.booking.mapper;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingMapperTest {

    private final BookingMapper bookingMapper;

    @Test
    public void toModelNullTest() {
        assertThat(bookingMapper.toModel(null, null, null)).isNull();
    }

    @Test
    public void toDtoNullTest() {
        assertThat(bookingMapper.toDto((Booking) null)).isNull();
        Booking booking = new Booking();
        assertThat(bookingMapper.toDto(booking)).isNotNull();
    }

    @Test
    public void toDtoListNullTest() {
        assertThat(bookingMapper.toDto((List<Booking>) null)).isNull();
    }
}