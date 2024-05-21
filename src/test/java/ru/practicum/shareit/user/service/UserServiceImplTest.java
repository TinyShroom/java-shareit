package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exception.ErrorMessages;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(
        properties = {"db.url=jdbc:h2:mem:test;MODE=PostgreSQL", "db.driver=org.h2.Driver"},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplTest {

    private final UserService userService;

    private UserDto userDto;
    private final EntityManager em;

    @BeforeEach
    public void setUp() {
        userDto = UserDto.builder()
                .name("username")
                .email("user@mail.com")
                .build();
    }

    @Test
    public void createOk() {
        var user = userService.create(userDto);
        var query = em.createQuery("select u from User u where u.id = :id", User.class);
        var result = query.setParameter("id", user.getId())
                .getSingleResult();

        assertThat(result.getId(), equalTo(user.getId()));
        assertThat(result.getName(), equalTo(userDto.getName()));
        assertThat(result.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    public void createExistEmailFail() {
        userService.create(userDto);
        assertThrows(DataIntegrityViolationException.class, () -> userService.create(userDto));
    }

    @Test
    public void findByIdOk() {
        var userCreate = userService.create(userDto);

        var user = userService.findById(userCreate.getId());

        assertThat(user.getId(), equalTo(userCreate.getId()));
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    public void findByIdUnknownUserIdOk() {
        var unknownUserId = 1L;

        var exception = assertThrows(NotFoundException.class, () -> userService.findById(unknownUserId));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(unknownUserId)));
    }

    @Test
    public void findAllOk() {
        var result = userService.getAll();
        assertThat(result, hasSize(0));

        var userCreate = userService.create(userDto);
        var userSecondDto = UserDto.builder()
                .name("second name")
                .email("second@mail.com")
                .build();
        var secondCreate = userService.create(userSecondDto);
        result = userService.getAll();
        assertThat(result, hasSize(2));
        assertThat(result.get(0).getId(), equalTo(userCreate.getId()));
        assertThat(result.get(0).getName(), equalTo(userDto.getName()));
        assertThat(result.get(0).getEmail(), equalTo(userDto.getEmail()));
        assertThat(result.get(1).getId(), equalTo(secondCreate.getId()));
        assertThat(result.get(1).getName(), equalTo(userSecondDto.getName()));
        assertThat(result.get(1).getEmail(), equalTo(userSecondDto.getEmail()));
    }

    @Test
    public void updateOk() {
        var user = userService.create(userDto);
        var newDto = UserDto.builder()
                .name("new name")
                .build();

        var result = userService.update(user.getId(), newDto);
        assertThat(result.getId(), equalTo(user.getId()));
        assertThat(result.getName(), equalTo(newDto.getName()));
        assertThat(result.getEmail(), equalTo(userDto.getEmail()));

        var updateDto = UserDto.builder()
                .name("updated name")
                .email("updated@mail.com")
                .build();

        result = userService.update(user.getId(), updateDto);
        assertThat(result.getId(), equalTo(user.getId()));
        assertThat(result.getName(), equalTo(updateDto.getName()));
        assertThat(result.getEmail(), equalTo(updateDto.getEmail()));
    }

    @Test
    public void updateUnknownUserFail() {
        var updateDto = UserDto.builder()
                .name("updated name")
                .email("updated@mail.com")
                .build();

        var unknownUserId = 1L;
        var exception = assertThrows(NotFoundException.class, () -> userService.update(unknownUserId, updateDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(unknownUserId)));
    }

    @Test
    public void deleteOk() {
        var user = userService.create(userDto);

        userService.delete(user.getId());

        var exception = assertThrows(NotFoundException.class, () -> userService.findById(user.getId()));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(user.getId())));
    }

}