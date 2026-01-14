package ru.practicum.user.service;

import ru.practicum.user.model.UserDto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto);

    List<UserDto> getUsers(List<Long> ids, Long from, Long size);

    void deleteUser(Long userId);
}
