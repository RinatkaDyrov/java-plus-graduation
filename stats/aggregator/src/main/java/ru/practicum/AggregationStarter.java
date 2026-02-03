package ru.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.service.AggregatorService;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final Consumer<String, SpecificRecordBase> consumer;
    private final Producer<String, SpecificRecordBase> producer;
    private final AggregatorService aggregatorService;

    @Value("${kafka.topics.user-action}")
    private String userActionTopic;

    @Value("${kafka.topics.event-similarity}")
    private String eventSimilarityTopic;

    private static final long POLL_TIMEOUT = 1;

    public void start() {
        consumer.subscribe(List.of(userActionTopic));

        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(Duration.ofSeconds(POLL_TIMEOUT));
                    for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                        try {
                            List<EventSimilarityAvro> list = aggregatorService
                                    .aggregate(record.value());

                            if (list.isEmpty()) {
                                continue;
                            }

                            list.forEach(x -> producer.send(new ProducerRecord<>(
                                    eventSimilarityTopic,
                                    null,
                                    x.getEventA() + "_" + x.getEventB(),
                                    x)));
                        } catch (Exception e) {
                            log.error("Ошибка обработки сообщения с ключом: {}", record.key(), e);

                        }
                    }
                    consumer.commitSync();
                }

            } catch (WakeupException ignored) {
                log.warn("Прервано ожидание потока {}", Thread.currentThread().getName());
            } catch (Exception e) {
                log.error("Ошибка во время обработки событий от датчиков", e);
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }, "aggregation-thread");
        thread.start();
    }
}
