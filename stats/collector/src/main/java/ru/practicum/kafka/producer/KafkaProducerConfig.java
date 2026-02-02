package ru.practicum.kafka.producer;

import jakarta.annotation.PreDestroy;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Properties;

@Configuration
public class KafkaProducerConfig {
    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.value-serializer}")
    private String valueSerializer;

    @Bean
    public Producer<String, SpecificRecordBase> kafkaProducer() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 5);
        config.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);

        return new KafkaProducer<>(config);
    }

    @Bean
    public EventProducer eventProducer(Producer<String, SpecificRecordBase> producer) {
        return new EventProducer(producer);
    }

    public static class EventProducer {
        private final Producer<String, SpecificRecordBase> producer;

        public EventProducer(Producer<String, SpecificRecordBase> producer) {
            this.producer = producer;
        }

        public Producer<String, SpecificRecordBase> getProducer() {
            return producer;
        }

        @PreDestroy
        public void close() {
            producer.flush();
            producer.close(Duration.ofSeconds(10));
        }
    }
}
