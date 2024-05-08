package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto user) {
        return UserMapper.userToDto(userRepository.create(UserMapper.dtoToUser(user)));
    }

    @Override
    public UserDto findById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("user with id == %d not found", id)));
        return UserMapper.userToDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return UserMapper.usersToDto(userRepository.getAll());
    }

    @Override
    public UserDto update(long id, UserDto userDto) {
        userDto.setId(id);
        var user = userRepository.update(UserMapper.dtoToUser(userDto))
                .orElseThrow(() -> new NotFoundException(String.format("user with id == %d not found", id)));
        return UserMapper.userToDto(user);
    }

    @Override
    public void delete(Long id) {
        userRepository.delete(id)
                .orElseThrow(() -> new NotFoundException(String.format("user with id == %d not found", id)));
    }
}
