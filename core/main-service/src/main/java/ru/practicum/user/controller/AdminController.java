package ru.practicum.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.user.model.UserDto.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final UserService userService;

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody UserDto userDto) {
        log.info("Запрос на добавление пользователя с параметрами {}", userDto);
        return userService.createUser(userDto);
    }

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUsers(@RequestParam(required = false, defaultValue = "") List<Long> ids,
                                  @RequestParam(required = false, defaultValue = "0") @Min(0) Long from,
                                  @RequestParam(required = false, defaultValue = "10") @Min(1) Long size) {
        log.info("Запрос на получение всех пользователей удовлетворяющему условию: ids {}, from {}, size {}",
                ids, from, size);
        return userService.getUsers(ids, from, size);
    }


    @DeleteMapping("users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }


}
