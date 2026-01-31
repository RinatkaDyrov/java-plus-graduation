package ru.practicum.feignClient.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.dto.user.UserDto;

import java.util.Optional;

@FeignClient(name = "USER-SERVICE", fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/admin/users/{userId}")
    Optional<UserDto> getUserById(@PathVariable Long userId);
}
