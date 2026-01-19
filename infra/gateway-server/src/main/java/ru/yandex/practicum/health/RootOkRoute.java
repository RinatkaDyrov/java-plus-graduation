package ru.yandex.practicum.health;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RootOkRoute {
    @Bean
    RouterFunction<ServerResponse> root() {
        return route(GET("/"), req ->
                ServerResponse.ok()
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValue("OK"));
    }
}
