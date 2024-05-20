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
    private final UserMapper userMapper;

    @Override
    public UserDto create(UserDto user) {
        return userMapper.userToDto(userRepository.save(userMapper.dtoToUser(user)));
    }

    @Override
    public UserDto findById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("user with id == %d not found", id)));
        return userMapper.userToDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userMapper.usersToDto(userRepository.findAll());
    }

    @Override
    public UserDto update(long id, UserDto userDto) {
        var oldUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("user with id == %d not found", id)));
        userDto.setId(id);
        userMapper.dtoToUser(oldUser, userDto);
        var user = userRepository.save(oldUser);
        return userMapper.userToDto(user);
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
