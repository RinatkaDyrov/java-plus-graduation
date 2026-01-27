package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.feignClient.event.EventClient;
import ru.practicum.feignClient.user.UserClient;

@SpringBootApplication
@EnableFeignClients(clients = {EventClient.class, UserClient.class})
public class EventRequestApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventRequestApplication.class, args);
    }
}