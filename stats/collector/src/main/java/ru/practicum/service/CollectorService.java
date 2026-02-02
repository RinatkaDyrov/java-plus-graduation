package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.kafka.producer.KafkaEventProducer;

@Service
@Slf4j
@RequiredArgsConstructor
public class CollectorService {

    private final KafkaEventProducer producer;

    public void send(String topic, UserActionAvro message) {
        producer.sendWithReport(topic, message.getUserId(), message);
    }
}
