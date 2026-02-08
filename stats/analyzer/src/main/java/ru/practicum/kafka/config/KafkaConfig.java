package ru.practicum.kafka.config;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Configuration
public class KafkaConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.consumer.key-deserializer}")
    private String keyDeserializer;

    @Value("${kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    @Value("${kafka.consumer.enable-auto-commit}")
    private boolean enableAutoCommit;

    @Value("${kafka.consumer.actions.group-id}")
    private String actionsGroupId;

    @Value("${kafka.consumer.actions.value-deserializer}")
    private String actionsValueDeserializer;

    @Value("${kafka.consumer.events.group-id}")
    private String eventsGroupId;

    @Value("${kafka.consumer.events.value-deserializer}")
    private String eventsValueDeserializer;

    @Bean
    public Properties actionsConsumerProperties() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, actionsValueDeserializer);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, actionsGroupId);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 200);
        return properties;
    }

    @Bean
    public Properties eventSimilarityConsumerProperties() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, eventsValueDeserializer);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, eventsGroupId);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50);
        return properties;
    }

    @Bean(name = "userActionKafkaConsumer")
    public Consumer<String, UserActionAvro> userActionConsumer() {
        return new KafkaConsumer<>(actionsConsumerProperties());
    }

    @Bean(name = "eventSimilarityKafkaConsumer")
    public Consumer<String, EventSimilarityAvro> eventSimilarityConsumer() {
        return new KafkaConsumer<>(eventSimilarityConsumerProperties());
    }
}
