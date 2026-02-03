package ru.practicum.kafka;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KafkaEventSimilarityProducer {

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
        config.put(ProducerConfig.RETRIES_CONFIG, 5);
        config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        config.put(ProducerConfig.ACKS_CONFIG, "all");

        return new KafkaProducer<>(config);
    }

//    @Bean
//    public SensorSnapshotProducer eventProducer(Producer<String, SpecificRecordBase> producer) {
//        return new SensorSnapshotProducer(producer);
//    }
//
//    public static class SensorSnapshotProducer {
//        private final Producer<String, SpecificRecordBase> producer;
//
//        public SensorSnapshotProducer(Producer<String, SpecificRecordBase> producer) {
//            this.producer = producer;
//        }
//
//        public Producer<String, SpecificRecordBase> getProducer() {
//            return producer;
//        }
//
//        @PreDestroy
//        public void close() {
//            producer.flush();
//            producer.close(Duration.ofSeconds(10));
//        }
//    }
}
