package ru.practicum.shareit.user.utils;

import ru.practicum.shareit.user.model.User;

public class UserUpdater {

    public static void update(User oldUser, User newUser) {
        if (newUser.getName() != null) {
            oldUser.setName(newUser.getName());
        }
        if (newUser.getEmail() != null) {
            oldUser.setEmail(newUser.getEmail());
        }
    }
}
