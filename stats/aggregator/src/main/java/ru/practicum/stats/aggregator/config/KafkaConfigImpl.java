package ru.practicum.stats.aggregator.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = "stats.aggregator.kafka")
@Slf4j
@Setter
public class KafkaConfigImpl implements KafkaConfig {
    private UserActions userActions;
    private EventsSimilarity eventsSimilarity;

    @Getter
    @Setter
    private static class UserActions {
        List<String> topics;
        Consumer consumer;

        @Getter
        @Setter
        private static class Consumer {
            private Long pollTimeout;
            private Properties properties;
        }
    }

    @Getter
    @Setter
    private static class EventsSimilarity {
        List<String> topics;
        Producer producer;

        @Getter
        @Setter
        private static class Producer {
            private Properties properties;
        }
    }

    @Override
    public Duration userActionsPollTimeout() {
        return Duration.ofMillis(userActions.consumer.getPollTimeout());
    }

    @Override
    public List<String> userActionsTopics() {
        return userActions.topics;
    }

    @Override
    public List<String> eventSimilarityTopics() {
        return eventsSimilarity.topics;
    }

    private Consumer<Long, UserActionAvro> consumer;

    private Producer<String, EventSimilarityAvro> producer;

    @Override
    public Consumer<Long, UserActionAvro> userActionsConsumer() {
        if (consumer == null) {
            consumer = new KafkaConsumer<>(userActions.consumer.getProperties());
            log.info("Создали косьюмер с groupId = {}", userActions.consumer.getProperties().get("group.id"));
        }
        return consumer;
    }

    @Override
    public Producer<String, EventSimilarityAvro> eventSimilarityProducer() {
        if (producer == null) {
            producer = new KafkaProducer<>(eventsSimilarity.producer.getProperties());
            log.info("Создали продюсер");
        }
        return producer;
    }

    @Override
    public void stop() {
        if (consumer != null) {
            log.info("Закрываем косьюмер");
            consumer.close();
        }
        if (producer != null) {
            log.info("Закрываем продюсер");
            producer.close();
        }
    }

}
