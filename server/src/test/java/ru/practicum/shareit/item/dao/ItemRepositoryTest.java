package ru.practicum.shareit.item.dao;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.util.PageRequestWithOffset;

import javax.persistence.EntityManager;
import java.util.stream.Collectors;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRepositoryTest {

    private final ItemRepository itemRepository;
    private final EntityManager em;

    @Test
    public void searchOk() {
        var text = "text";
        var items = em.createQuery("select i from Item i where" +
                        " lower(i.name) like lower(concat('%', :text, '%')) or" +
                        " lower(i.description) like lower(concat('%', :text, '%'))", Item.class)
                .setParameter("text", text)
                .getResultStream()
                .filter(Item::getAvailable)
                .collect(Collectors.toList());

        var result = itemRepository.search(text, Pageable.unpaged());
        Assertions.assertThat(result).hasSize(items.size());
        Assertions.assertThat(result).usingRecursiveComparison().isEqualTo(items);

        var from = 2;
        var size = 2;
        items = items.stream()
                .skip(from / size * size)
                .limit(size)
                .collect(Collectors.toList());
        result = itemRepository.search(text, PageRequestWithOffset.of(from, size));
        Assertions.assertThat(result).hasSize(items.size());
        Assertions.assertThat(result).usingRecursiveComparison().isEqualTo(items);
    }
}