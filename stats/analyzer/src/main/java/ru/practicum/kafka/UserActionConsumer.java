package ru.practicum.kafka;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.handler.UserActionHandler;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class UserActionConsumer implements Runnable {

    private final Consumer<String, UserActionAvro> consumer;
    private final UserActionHandler userActionHandler;
    private boolean isRunning = true;

    @Autowired
    public UserActionConsumer(@Qualifier("userActionKafkaConsumer")
                              Consumer<String, UserActionAvro> consumer,
                              UserActionHandler userActionHandler) {
        this.consumer = consumer;
        this.userActionHandler = userActionHandler;
    }

    @Value("${kafka.consumer.actions.topic}")
    private String topicUserAction;

    @Value("${kafka.consumer.poll-timeout-ms}")
    private int pollTimeout;

    @Override
    public void run() {
        try {
            consumer.subscribe(List.of(topicUserAction));
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            while (isRunning) {
                ConsumerRecords<String, UserActionAvro> records =
                        consumer.poll(Duration.ofMillis(pollTimeout));

                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    UserActionAvro action = record.value();
                    log.info("Полученные данные действий пользователя: {}", action);
                    userActionHandler.handle(action);
                }

                consumer.commitAsync();
            }

        } catch (WakeupException wakeupException) {
            log.info("WakeupException for events-similarity consumer");
        } catch (Exception e) {
            log.error("Ошибка при чтении данных из топика {}", topicUserAction, e);
        } finally {
            consumer.close();
            log.info("Потребитель user-action закрыт");

        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Прекращение работы потребителя");
        isRunning = false;
        if (consumer != null) {
            try {
                consumer.commitSync();
            } finally {
                consumer.close(Duration.ofSeconds(5));
            }
        }
    }
}
