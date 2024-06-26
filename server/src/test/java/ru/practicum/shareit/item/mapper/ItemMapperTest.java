package ru.practicum.shareit.item.mapper;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemMapperTest {

    private final ItemMapper itemMapper;

    @Test
    public void toModelNullTest() {
        assertThat(itemMapper.toModel(null, null, null)).isNull();
    }

    @Test
    public void toDtoNullTest() {
        assertThat(itemMapper.toDto((Item) null)).isNull();
    }

    @Test
    public void toDtoListNullTest() {
        assertThat(itemMapper.toDto((List<Item>) null)).isNull();
    }

}