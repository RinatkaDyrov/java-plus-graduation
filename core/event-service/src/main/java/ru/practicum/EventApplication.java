package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.feignClient.eventRequest.EventRequestClient;
import ru.practicum.feignClient.eventRequest.LikeRequestClient;
import ru.practicum.feignClient.user.UserClient;

@SpringBootApplication
@EnableFeignClients(clients = {UserClient.class, EventRequestClient.class, LikeRequestClient.class})
public class EventApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventApplication.class, args);
    }
}
