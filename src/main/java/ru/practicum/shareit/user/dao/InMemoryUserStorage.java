package ru.practicum.shareit.user.dao;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.utils.UserUpdater;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users;
    private final Set<String> emails;

    private long idCounter;

    public InMemoryUserStorage() {
        users = new HashMap<>();
        emails = new HashSet<>();
    }

    @Override
    public User add(User user) {
        if (!emails.add(user.getEmail())) {
            throw new DuplicateEmailException(String.format("Email '%s' already exist", user.getEmail()));
        }
        user.setId(idGenerator());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> update(long id, User user) {
        var oldUser = users.get(id);
        if (oldUser == null) return Optional.empty();
        if (user.getEmail() != null && !oldUser.getEmail().equals(user.getEmail()) && !emails.add(user.getEmail())) {
            throw new DuplicateEmailException(String.format("Email '%s' already exist", user.getEmail()));
        }
        emails.remove(oldUser.getEmail());
        UserUpdater.update(oldUser, user);
        emails.add(oldUser.getEmail());
        return Optional.of(oldUser);
    }

    @Override
    public Optional<User> delete(Long id) {
        var user = users.remove(id);
        if (user == null) return Optional.empty();
        emails.remove(user.getEmail());
        return Optional.of(user);
    }

    private long idGenerator() {
        return ++idCounter;
    }
}
