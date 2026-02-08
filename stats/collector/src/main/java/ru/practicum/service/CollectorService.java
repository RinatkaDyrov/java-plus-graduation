package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.practicum.kafka.producer.KafkaEventProducerConfig;

@Service
@Slf4j
@RequiredArgsConstructor
public class CollectorService {

    private final KafkaEventProducerConfig.EventProducer producer;

    public void send(String topic, SpecificRecordBase message) {
        producer.getProducer().send(new ProducerRecord<>(topic, message), (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка при отправке в Kafka: {}", exception.getMessage(), exception);
            } else {
                log.debug("Сообщение записано в {}: offset={}, partition={}",
                        metadata.topic(), metadata.offset(), metadata.partition());
            }
        });
    }
}
