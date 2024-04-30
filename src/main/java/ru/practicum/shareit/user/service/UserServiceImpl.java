package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    @Override
    public UserDto add(UserDto user) {
        return UserMapper.userToDto(userStorage.add(UserMapper.dtoToUser(user)));
    }

    @Override
    public UserDto findById(Long id) {
        var user = userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("user with id == %d not found", id)));
        return UserMapper.userToDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return UserMapper.usersToDto(userStorage.getAll());
    }

    @Override
    public UserDto update(long id, UserDto userDto) {
        var user = userStorage.update(id, UserMapper.dtoToUser(userDto))
                .orElseThrow(() -> new NotFoundException(String.format("user with id == %d not found", id)));
        return UserMapper.userToDto(user);
    }

    @Override
    public void delete(Long id) {
        userStorage.delete(id)
                .orElseThrow(() -> new NotFoundException(String.format("user with id == %d not found", id)));
    }
}
