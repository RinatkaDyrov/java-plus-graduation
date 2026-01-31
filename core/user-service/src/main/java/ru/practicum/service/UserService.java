package ru.practicum.service;

import ru.practicum.dto.user.UserDto;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDto createUser(UserDto userDto);

    List<UserDto> getUsers(List<Long> ids, Long from, Long size);

    void deleteUser(Long userId);

    Optional<UserDto> gtUserById(Long id);
}
