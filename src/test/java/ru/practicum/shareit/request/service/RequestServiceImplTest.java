package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.ErrorMessages;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.Request;
import ru.practicum.shareit.request.dto.RequestCreateDto;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.util.PageRequestWithOffset;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class RequestServiceImplTest {

    private final RequestService requestService;
    private final RequestMapper requestMapper;
    private final ItemMapper itemMapper;
    private final EntityManager em;

    private final long ownerId = 1;
    private final long bookerId = 2;
    private final long userId = 3;
    private final long unknownUserId = 100;

    private final long requestWithoutItemsId = 1;
    private final long requestWithItemsId = 2;
    private final long unknownRequestId = 100;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void createOk() {
        var requestCreateDto = RequestCreateDto.builder()
                .description("rq description")
                .build();
        var requestDto = requestService.create(bookerId, requestCreateDto);
        var query = em.createQuery("select r from Request r where r.id = :id", Request.class);
        var request = query.setParameter("id", requestDto.getId())
                .getSingleResult();

        assertThat(request.getId(), equalTo(requestDto.getId()));
        assertThat(request.getDescription(), equalTo(requestCreateDto.getDescription()));
        assertThat(request.getCreated(), equalTo(requestDto.getCreated()));
        assertThat(request.getUser().getId(), equalTo(bookerId));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void createWithoutUserFail() {
        var requestCreateDto = RequestCreateDto.builder()
                .description("rq description")
                .build();
        var exception = assertThrows(NotFoundException.class, () -> requestService.create(unknownUserId, requestCreateDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(unknownUserId)));
    }

    @Test
    void findByIdOk() {

        var requestQuery = em.createQuery("select r from Request r where r.id = :id", Request.class);
        var request = requestQuery.setParameter("id", requestWithoutItemsId)
                .getSingleResult();

        var itemsQuery = em.createQuery("select i from Item i where i.request.id = :id", Item.class);
        var items = itemsQuery.setParameter("id", requestWithoutItemsId)
                .getResultStream()
                .map(itemMapper::toItemWithRequestDto)
                .collect(Collectors.toList());

        var serviceRequest = requestService.findById(bookerId, requestWithoutItemsId);

        assertThat(serviceRequest.getId(), equalTo(requestWithoutItemsId));
        assertThat(serviceRequest.getDescription(), equalTo(request.getDescription()));
        assertThat(serviceRequest.getCreated(), equalTo(request.getCreated()));
        assertThat(serviceRequest.getItems(), hasSize(items.size()));
        org.assertj.core.api.Assertions.assertThat(serviceRequest.getItems())
                .usingRecursiveComparison()
                .isEqualTo(items);

        request = requestQuery.setParameter("id", requestWithItemsId)
                .getSingleResult();
        items = itemsQuery.setParameter("id", requestWithItemsId)
                .getResultStream()
                .map(itemMapper::toItemWithRequestDto)
                .collect(Collectors.toList());

        serviceRequest = requestService.findById(bookerId, requestWithItemsId);

        assertThat(serviceRequest.getId(), equalTo(request.getId()));
        assertThat(serviceRequest.getDescription(), equalTo(request.getDescription()));
        assertThat(serviceRequest.getCreated(), equalTo(request.getCreated()));

        org.assertj.core.api.Assertions.assertThat(serviceRequest.getItems())
                .usingRecursiveComparison()
                .isEqualTo(items);
    }

    @Test
    void findByIdWithoutUserFail() {
        var exception = assertThrows(NotFoundException.class,
                () -> requestService.findById(unknownUserId, requestWithItemsId));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(unknownUserId)));
    }

    @Test
    void findByIdWithoutRequestFail() {
        var exception = assertThrows(NotFoundException.class,
                () -> requestService.findById(bookerId, unknownRequestId));
        assertThat(exception.getMessage(), equalTo(
                ErrorMessages.REQUEST_NOT_FOUND.getFormatMessage(unknownRequestId)));
    }

    @Test
    void findByUserIdOk() {
        var requestQuery = em.createQuery("select r from Request r where r.user.id = :id", Request.class);
        var requests = requestQuery.setParameter("id", bookerId)
                .getResultList();

        var itemsQuery = em.createQuery("select i from Item i where i.request.id in :id", Item.class);
        var items = itemsQuery.setParameter("id", requests.stream().map(Request::getId).collect(Collectors.toList()))
                .getResultList().stream()
                .collect(Collectors.groupingBy(i -> i.getRequest().getId(),
                        Collectors.mapping(itemMapper::toItemWithRequestDto, Collectors.toList())));
        var requestWithItemsDto = requests.stream()
                .sorted(Comparator.comparing(Request::getCreated).reversed())
                .map(r -> requestMapper.toRequestWithItemsDto(r, items.getOrDefault(r.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
        var serviceRequest = requestService.findByUserId(bookerId);

        assertThat(serviceRequest.size(), equalTo(requests.size()));
        org.assertj.core.api.Assertions.assertThat(serviceRequest)
                .usingRecursiveComparison()
                .isEqualTo(requestWithItemsDto);
    }

    @Test
    void findByUserIdWithoutUserFail() {
        var exception = assertThrows(NotFoundException.class,
                () -> requestService.findByUserId(unknownUserId));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(unknownUserId)));
    }

    @Test
    void findAllOk() {
        var from = 0;
        var size = 10;
        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("created").descending());
        var result = requestService.findAll(bookerId, pageable);
        assertThat(result, hasSize(0));

        size = 1;
        from = 1;
        var requestQuery = em.createQuery("select r from Request r where r.user.id <> :id", Request.class);
        var requests = requestQuery.setParameter("id", userId)
                .getResultList();

        var itemsQuery = em.createQuery("select i from Item i where i.request.id in :id", Item.class);
        var items = itemsQuery.setParameter("id", requests.stream().map(Request::getId).collect(Collectors.toList()))
                .getResultList().stream()
                .collect(Collectors.groupingBy(i -> i.getRequest().getId(),
                        Collectors.mapping(itemMapper::toItemWithRequestDto, Collectors.toList())));
        var requestWithItemsDto = requests.stream()
                .sorted(Comparator.comparing(Request::getCreated).reversed())
                .skip(from / size * size)
                .limit(size)
                .map(r -> requestMapper.toRequestWithItemsDto(r, items.getOrDefault(r.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
        pageable = PageRequestWithOffset.of(from, size, Sort.by("created").descending());
        result = requestService.findAll(ownerId, pageable);

        assertThat(result, hasSize(size));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(requestWithItemsDto);
    }

    @Test
    void findAllWithoutUserFail() {
        var from = 0;
        var size = 10;
        Pageable pageable = PageRequestWithOffset.of(from, size, Sort.by("created").descending());
        var exception = assertThrows(NotFoundException.class,
                () -> requestService.findAll(unknownUserId, pageable));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(unknownUserId)));
    }
}