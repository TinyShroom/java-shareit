package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.ErrorMessages;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserServiceImplTest {

    private final UserService userService;
    private final UserMapper userMapper;
    private final EntityManager em;

    private UserDto userDto;

    private final long ownerId = 1;
    private final long userId = 3;
    private final long unknownUserId = 100;

    @BeforeEach
    public void setUp() {
        userDto = UserDto.builder()
                .name("user created name")
                .email("usercreated@mail.com")
                .build();
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
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
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void createExistEmailFail() {
        var query = em.createQuery("select u from User u where u.id = :id", User.class);
        var result = query.setParameter("id", ownerId)
                .getSingleResult();
        userDto.setEmail(result.getEmail());
        assertThrows(DataIntegrityViolationException.class, () -> userService.create(userDto));
    }

    @Test
    public void findByIdOk() {

        var query = em.createQuery("select u from User u where u.id = :id", User.class);
        var result = query.setParameter("id", ownerId)
                .getSingleResult();

        var user = userService.findById(ownerId);

        assertThat(user.getId(), equalTo(ownerId));
        assertThat(user.getName(), equalTo(result.getName()));
        assertThat(user.getEmail(), equalTo(result.getEmail()));
    }

    @Test
    public void findByIdUnknownUserIdOk() {
        var exception = assertThrows(NotFoundException.class, () -> userService.findById(unknownUserId));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(unknownUserId)));
    }

    @Test
    public void findAllOk() {

        var query = em.createQuery("select u from User u", User.class);
        var users = query.getResultStream()
                .map(userMapper::userToDto)
                .collect(Collectors.toList());

        var result = userService.getAll();
        assertThat(result.size(), equalTo(users.size()));
        org.assertj.core.api.Assertions.assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(users);

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void updateOk() {
        var query = em.createQuery("select u from User u where u.id = :id", User.class);
        var resultBefore = query.setParameter("id", userId)
                .getSingleResult();
        var newName = "new " + resultBefore.getName();
        var newUser = UserDto.builder()
                .name(newName)
                .build();
        var result = userService.update(userId, newUser);

        assertThat(result.getId(), equalTo(userId));
        assertThat(result.getName(), equalTo(newName));
        assertThat(result.getEmail(), equalTo(resultBefore.getEmail()));

        var newEmail = "new" + result.getEmail();
        newUser = UserDto.builder()
                .email(newEmail)
                .build();

        result = userService.update(userId, newUser);
        assertThat(result.getId(), equalTo(userId));
        assertThat(result.getName(), equalTo(newName));
        assertThat(result.getEmail(), equalTo(newEmail));

        newName = "updated " + result.getName();
        newEmail = "updated" + result.getEmail();
        newUser = UserDto.builder()
                .name(newName)
                .email(newEmail)
                .build();

        result = userService.update(userId, newUser);
        assertThat(result.getId(), equalTo(userId));
        assertThat(result.getName(), equalTo(newName));
        assertThat(result.getEmail(), equalTo(newEmail));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void updateUnknownUserFail() {
        var updateDto = UserDto.builder()
                .name("updated name")
                .email("updated@mail.com")
                .build();

        var exception = assertThrows(NotFoundException.class, () -> userService.update(unknownUserId, updateDto));
        assertThat(exception.getMessage(), equalTo(ErrorMessages.USER_NOT_FOUND.getFormatMessage(unknownUserId)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void deleteOk() {
        var query = em.createQuery("select u from User u where u.id = :id", User.class);
        var resultBefore = query.setParameter("id", userId)
                .getSingleResult();
        userService.delete(resultBefore.getId());
        assertThrows(NoResultException.class, () -> query.setParameter("id", userId).getSingleResult());
    }

}