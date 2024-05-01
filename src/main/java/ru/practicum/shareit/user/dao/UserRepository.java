package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User create(User user);

    Optional<User> findById(Long id);

    List<User> getAll();

    Optional<User> update(User user);

    Optional<User> delete(Long id);
}
