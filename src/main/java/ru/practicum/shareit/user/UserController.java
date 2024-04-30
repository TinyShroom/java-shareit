package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constraint.AddingConstraint;
import ru.practicum.shareit.constraint.PatchConstraint;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto add(@RequestBody @Validated(AddingConstraint.class) UserDto user) {
        log.info("POST /users: {}", user.toString());
        var result = userService.add(user);
        log.info("completion POST /users: {}", result);
        return result;
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable long id) {
        log.info("GET /user: {}", id);
        var user = userService.findById(id);
        log.info("completion GET /user: {}", user);
        return user;
    }

    @GetMapping
    public List<UserDto> getAll() {
        log.info("GET /users: all");
        var result = userService.getAll();
        log.info("completion GET /users: size {}", result.size());
        return result;
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable long id, @RequestBody @Validated(PatchConstraint.class) UserDto user) {
        log.info("PUT /users: {}", user.toString());
        var result = userService.update(id, user);
        log.info("completion PUT /users: {}", result);
        return result;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        log.info("DELETE /users: {}", id);
        userService.delete(id);
        log.info("completion DELETE /users: success");
    }
}
