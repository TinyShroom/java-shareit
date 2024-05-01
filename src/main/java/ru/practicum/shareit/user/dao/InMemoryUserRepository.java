package ru.practicum.shareit.user.dao;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> users;
    private final Set<String> emails;

    private long idCounter;

    public InMemoryUserRepository() {
        users = new HashMap<>();
        emails = new HashSet<>();
    }

    @Override
    public User create(User user) {
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
    public Optional<User> update(User user) {
        var oldUser = users.get(user.getId());
        if (oldUser == null) return Optional.empty();
        if (user.getEmail() != null && !oldUser.getEmail().equals(user.getEmail()) && !emails.add(user.getEmail())) {
            throw new DuplicateEmailException(String.format("Email '%s' already exist", user.getEmail()));
        }
        emails.remove(oldUser.getEmail());
        updateUser(oldUser, user);
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

    private static void updateUser(User oldUser, User newUser) {
        if (newUser.getName() != null) {
            oldUser.setName(newUser.getName());
        }
        if (newUser.getEmail() != null) {
            oldUser.setEmail(newUser.getEmail());
        }
    }

}
