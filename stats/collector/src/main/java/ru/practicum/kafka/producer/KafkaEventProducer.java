package ru.practicum.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducer {

    private final KafkaTemplate<Long, byte[]> kafkaTemplate;

    public void sendWithReport(String topic, Long key, SpecificRecord record) {
        kafkaTemplate.send(topic, key, serialize(record))
                .whenComplete(((stringSendResult, throwable) -> {
                    if (throwable != null) {
                        log.warn("Ошибка при отправке сообщения в топик {}: {}", topic, throwable.getMessage());
                    } else {
                        log.info("Сообщение отправлено в топик {}. Offset - {}",
                                topic, stringSendResult.getRecordMetadata().offset());
                    }
                }));
    }

    private byte[] serialize(SpecificRecord record) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            DatumWriter<SpecificRecord> datumWriter = new SpecificDatumWriter<>(record.getSchema());
            datumWriter.write(record, encoder);
            encoder.flush();

            return out.toByteArray();
        } catch (IOException e) {
            log.warn("Ошибка сериализации данных");
            throw new RuntimeException(e.getMessage());
        }
    }
}