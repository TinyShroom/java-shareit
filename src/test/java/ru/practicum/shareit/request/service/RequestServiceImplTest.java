package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.ErrorMessages;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.Request;
import ru.practicum.shareit.request.dto.RequestCreateDto;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(
        properties = {"db.url=jdbc:h2:mem:test;MODE=PostgreSQL", "db.driver=org.h2.Driver"},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class RequestServiceImplTest {

    private final RequestService requestService;
    private final UserService userService;
    private final ItemService itemService;

    private UserDto userDto;

    private UserDto ownerDto;

    private ItemDto itemCreateDto;

    private final EntityManager em;

    @BeforeEach
    public void setUp() {
        userDto = UserDto.builder()
                .name("username")
                .email("user@mail.com")
                .build();

        ownerDto = UserDto.builder()
                .name("ownername")
                .email("owner@mail.com")
                .build();

        itemCreateDto = ItemDto.builder()
                .name("item")
                .description("item_description")
                .available(true)
                .build();
    }

    @Test
    public void createOk() {
        var requestCreateDto = RequestCreateDto.builder()
                .description("rq description")
                .build();
        var user = userService.create(userDto);
        var requestDto = requestService.create(user.getId(), requestCreateDto);
        var query = em.createQuery("select r from Request r where r.id = :id", Request.class);
        var request = query.setParameter("id", requestDto.getId())
                .getSingleResult();

        assertThat(request.getId(), equalTo(requestDto.getId()));
        assertThat(request.getDescription(), equalTo(requestCreateDto.getDescription()));
        assertThat(request.getCreated(), equalTo(requestDto.getCreated()));
        assertThat(request.getUser().getId(), equalTo(user.getId()));
    }

    @Test
    void createWithoutUserFail() {
        var userId = 100;
        var requestCreateDto = RequestCreateDto.builder()
                .description("rq description")
                .build();
        var exception = assertThrows(NotFoundException.class, () -> requestService.create(userId, requestCreateDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(userId)));
    }

    @Test
    void findByIdOk() {
        var requestCreateDto = RequestCreateDto.builder()
                .description("rq description")
                .build();
        var owner = userService.create(ownerDto);
        var user = userService.create(userDto);

        var requestDto = requestService.create(user.getId(), requestCreateDto);

        var request = requestService.findById(user.getId(), requestDto.getId());

        assertThat(request.getId(), equalTo(requestDto.getId()));
        assertThat(request.getDescription(), equalTo(requestCreateDto.getDescription()));
        assertThat(request.getCreated(), equalTo(requestDto.getCreated()));
        assertThat(request.getItems(), hasSize(0));

        List<ItemDto> items = new ArrayList<>();
        itemCreateDto.setRequestId(requestDto.getId());

        for (var i = 0; i < 2; ++i) {
            items.add(itemService.create(owner.getId(), itemCreateDto));
        }

        request = requestService.findById(user.getId(), requestDto.getId());

        assertThat(request.getId(), equalTo(requestDto.getId()));
        assertThat(request.getDescription(), equalTo(requestCreateDto.getDescription()));
        assertThat(request.getCreated(), equalTo(requestDto.getCreated()));
        assertThat(request.getItems(), hasSize(items.size()));
    }

    @Test
    void findByIdWithoutUserFail() {
        var requestCreateDto = RequestCreateDto.builder()
                .description("rq description")
                .build();
        var user = userService.create(userDto);

        var requestDto = requestService.create(user.getId(), requestCreateDto);

        var unknownUserId = user.getId() + 1;

        var exception = assertThrows(NotFoundException.class,
                () -> requestService.findById(unknownUserId, requestDto.getId()));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(unknownUserId)));
    }

    @Test
    void findByIdWithoutRequestFail() {
        var user = userService.create(userDto);

        var unknownRequestId = 10;
        var exception = assertThrows(NotFoundException.class,
                () -> requestService.findById(user.getId(), unknownRequestId));
        assertThat(exception.getMessage(), equalTo(
                ErrorMessages.REQUEST_NOT_FOUND.getFormatMessage(unknownRequestId)));
    }

    @Test
    void findByUserIdOk() {
        var requestCreateDto = RequestCreateDto.builder()
                .description("rq description")
                .build();
        var owner = userService.create(ownerDto);
        var user = userService.create(userDto);

        var result = requestService.findByUserId(user.getId());
        assertThat(result, hasSize(0));

        List<RequestDto> requests = new ArrayList<>();
        requests.add(requestService.create(user.getId(), requestCreateDto));
        requestCreateDto.setDescription("new description");
        requests.add(requestService.create(user.getId(), requestCreateDto));

        requests.sort(Comparator.comparing(RequestDto::getCreated).reversed());

        itemCreateDto.setRequestId(requests.get(0).getId());
        List<ItemDto> items = new ArrayList<>();
        for (var i = 0; i < 2; ++i) {
            items.add(itemService.create(owner.getId(), itemCreateDto));
        }

        result = requestService.findByUserId(user.getId());

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getId(), equalTo(requests.get(0).getId()));
        assertThat(result.get(0).getDescription(), equalTo(requests.get(0).getDescription()));
        assertThat(result.get(0).getCreated(), equalTo(requests.get(0).getCreated()));
        assertThat(result.get(0).getItems(), hasSize(items.size()));
        assertThat(result.get(1).getId(), equalTo(requests.get(1).getId()));
        assertThat(result.get(1).getDescription(), equalTo(requests.get(1).getDescription()));
        assertThat(result.get(1).getCreated(), equalTo(requests.get(1).getCreated()));
        assertThat(result.get(1).getItems(), hasSize(0));
    }

    @Test
    void findByUserIdWithoutUserFail() {
        var unknownUserId = 1;

        var exception = assertThrows(NotFoundException.class,
                () -> requestService.findByUserId(unknownUserId));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(unknownUserId)));
    }

    @Test
    void findAllOk() throws InterruptedException {
        var owner = userService.create(ownerDto);
        var user = userService.create(userDto);

        var from = 0;
        Integer size = null;
        var result = requestService.findAll(owner.getId(), from, size);
        assertThat(result, hasSize(0));

        var requestCreateDto = RequestCreateDto.builder()
                .description("rq description")
                .build();
        List<RequestDto> requests = new ArrayList<>();
        requests.add(requestService.create(user.getId(), requestCreateDto));
        Thread.sleep(1);
        requestCreateDto = RequestCreateDto.builder()
                .description("new description")
                .build();
        requests.add(requestService.create(user.getId(), requestCreateDto));

        requests.sort(Comparator.comparing(RequestDto::getCreated).reversed());

        itemCreateDto.setRequestId(requests.get(0).getId());
        List<ItemDto> items = new ArrayList<>();
        for (var i = 0; i < 2; ++i) {
            items.add(itemService.create(owner.getId(), itemCreateDto));
        }

        result = requestService.findAll(owner.getId(), from, size);

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getId(), equalTo(requests.get(0).getId()));
        assertThat(result.get(0).getDescription(), equalTo(requests.get(0).getDescription()));
        assertThat(result.get(0).getCreated(), equalTo(requests.get(0).getCreated()));
        assertThat(result.get(0).getItems(), hasSize(items.size()));
        assertThat(result.get(1).getId(), equalTo(requests.get(1).getId()));
        assertThat(result.get(1).getDescription(), equalTo(requests.get(1).getDescription()));
        assertThat(result.get(1).getCreated(), equalTo(requests.get(1).getCreated()));
        assertThat(result.get(1).getItems(), hasSize(0));

        size = 1;
        from = 1;
        result = requestService.findAll(owner.getId(), from, size);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(requests.get(1).getId()));
        assertThat(result.get(0).getDescription(), equalTo(requests.get(1).getDescription()));
        assertThat(result.get(0).getCreated(), equalTo(requests.get(1).getCreated()));
        assertThat(result.get(0).getItems(), hasSize(0));

        result = requestService.findAll(user.getId(), from, size);
        assertThat(result, hasSize(0));
    }

    @Test
    void findAllWithoutUserFail() {
        var unknownUserId = 1;

        var exception = assertThrows(NotFoundException.class,
                () -> requestService.findAll(unknownUserId, 0, null));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(unknownUserId)));
    }
}