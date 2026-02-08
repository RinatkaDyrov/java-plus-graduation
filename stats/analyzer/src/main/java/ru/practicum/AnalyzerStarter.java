package ru.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.kafka.EventSimilarityConsumer;
import ru.practicum.kafka.UserActionConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerStarter implements CommandLineRunner {

    private final UserActionConsumer userActionService;
    private final EventSimilarityConsumer eventSimilarityService;

    @Override
    public void run(String... args) {
        Thread userActionThread = new Thread(userActionService);
        userActionThread.setName("userActionHandlerThread");
        userActionThread.start();
        log.info("Запущен поток userActionHandlerThread");

        Thread eventSimilarityThread = new Thread(eventSimilarityService);
        eventSimilarityThread.setName("eventSimilarityHandlerThread");
        eventSimilarityThread.start();
        log.info("Запущен поток eventSimilarityHandlerThread");
    }
}
