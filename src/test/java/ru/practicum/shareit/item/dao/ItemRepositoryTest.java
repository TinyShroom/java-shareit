package ru.practicum.shareit.item.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@DataJpaTest(properties = {"db.url=jdbc:h2:mem:test;MODE=PostgreSQL", "db.driver=org.h2.Driver"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRepositoryTest {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Test
    public void searchOk() {

        var text = "text";
        var result = itemRepository.search(text, Pageable.unpaged());
        assertThat(result, hasSize(0));

        var createOwner = new User();
        createOwner.setName("owner name");
        createOwner.setEmail("owner@mail.com");
        var owner = userRepository.save(createOwner);

        var createItem = new Item();
        createItem.setOwner(owner);
        createItem.setName("item");
        createItem.setDescription("item description");
        createItem.setAvailable(true);
        itemRepository.save(createItem);

        result = itemRepository.search(text, Pageable.unpaged());
        assertThat(result, hasSize(0));

        createItem.setName("it" + text + "em");
        var item = itemRepository.save(createItem);

        result = itemRepository.search(text, Pageable.unpaged());
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(item.getId()));
        assertThat(result.get(0).getName(), equalTo(item.getName()));
        assertThat(result.get(0).getDescription(), equalTo(item.getDescription()));
        assertThat(result.get(0).getAvailable(), equalTo(item.getAvailable()));
    }
}