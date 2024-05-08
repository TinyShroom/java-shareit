package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constraint.Create;
import ru.practicum.shareit.constraint.Update;
import ru.practicum.shareit.logging.Logging;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Logging
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody @Validated(Create.class) UserDto user) {
        return userService.create(user);
    }

    @Logging
    @GetMapping("/{id}")
    public UserDto getById(@PathVariable long id) {
        return userService.findById(id);
    }

    @Logging
    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll();
    }

    @Logging
    @PatchMapping("/{id}")
    public UserDto update(@PathVariable long id, @RequestBody @Validated(Update.class) UserDto user) {
        return userService.update(id, user);
    }

    @Logging
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        userService.delete(id);
    }
}
