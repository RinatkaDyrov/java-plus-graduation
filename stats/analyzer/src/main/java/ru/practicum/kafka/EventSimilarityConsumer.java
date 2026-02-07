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
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.handler.EventSimilarityHandler;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class EventSimilarityConsumer implements Runnable {

    private final Consumer<String, EventSimilarityAvro> consumer;
    private final EventSimilarityHandler eventSimilarityHandler;
    private boolean isRunning = true;

    @Autowired
    public EventSimilarityConsumer(@Qualifier("eventSimilarityKafkaConsumer")
                                   Consumer<String, EventSimilarityAvro> consumer,
                                   EventSimilarityHandler eventSimilarityHandler) {
        this.consumer = consumer;
        this.eventSimilarityHandler = eventSimilarityHandler;
    }

    @Value("${kafka.consumer.events.topic}")
    private String topic;

    @Value("${kafka.consumer.poll-timeout-ms}")
    private int pollTimeout;

    @Override
    public void run() {
        try {

            consumer.subscribe(List.of(topic));
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

            while (isRunning) {
                ConsumerRecords<String, EventSimilarityAvro> records =
                        consumer.poll(Duration.ofMillis(pollTimeout));

                for (ConsumerRecord<String, EventSimilarityAvro> record : records) {
                    EventSimilarityAvro eventSimilarity = record.value();
                    log.info("Полученные данные коэффициента схожести: {}", eventSimilarity);
                    eventSimilarityHandler.handle(eventSimilarity);
                }
                consumer.commitAsync();
            }
        } catch (WakeupException wakeupException) {
            log.info("WakeupException for events-similarity consumer");
        } catch (Exception e) {
            log.error("Ошибка при чтении данных из топика {}", topic, e);
        } finally {
            consumer.close();
            log.info("Потребитель events-similarity закрыт");
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
