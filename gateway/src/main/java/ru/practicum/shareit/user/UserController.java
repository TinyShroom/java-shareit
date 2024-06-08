package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constraint.Create;
import ru.practicum.shareit.constraint.Update;
import ru.practicum.shareit.logging.Logging;
import ru.practicum.shareit.user.dto.UserDto;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserClient userClient;

    @Logging
    @PostMapping
    public ResponseEntity<Object> create(@RequestBody @Validated(Create.class) UserDto user) {
        return userClient.create(user);
    }

    @Logging
    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable long id) {
        return userClient.getById(id);
    }

    @Logging
    @GetMapping
    public ResponseEntity<Object> getAll() {
        return userClient.getAll();
    }

    @Logging
    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable long id, @RequestBody @Validated(Update.class) UserDto user) {
        return userClient.update(id, user);
    }

    @Logging
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable long id) {
        return userClient.delete(id);
    }
}