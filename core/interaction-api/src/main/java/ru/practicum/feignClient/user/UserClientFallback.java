package ru.practicum.feignClient.user;

import ru.practicum.dto.user.UserDto;

import java.util.Optional;

public class UserClientFallback implements UserClient {

    @Override
    public Optional<UserDto> getUserById(Long userId) {
        return Optional.empty();
    }
}
