package ru.practicum.stats.analyzer.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = "stats.analyzer.kafka")
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
        Consumer consumer;

        @Getter
        @Setter
        private static class Consumer {
            private Long pollTimeout;
            private Properties properties;
        }
    }

    @Override
    public Duration userActionsPollTimeout() {
        return Duration.ofMillis(userActions.consumer.getPollTimeout());
    }

    @Override
    public Duration eventSimilarityPollTimeout() {
        return Duration.ofMillis(eventsSimilarity.consumer.getPollTimeout());
    }

    @Override
    public List<String> userActionsTopics() {
        return userActions.topics;
    }

    @Override
    public List<String> eventSimilarityTopics() {
        return eventsSimilarity.topics;
    }

    private Consumer<Long, UserActionAvro> userActionsConsumer;

    private Consumer<String, EventSimilarityAvro> eventsSimilarityConsumer;

    @Override
    public Consumer<Long, UserActionAvro> getUserActionsConsumer() {
        if (userActionsConsumer == null) {
            userActionsConsumer = new KafkaConsumer<>(userActions.consumer.getProperties());
            log.info("Создали косьюмер для user actions с groupId = {}", userActions.consumer.getProperties().get("group.id"));
        }
        return userActionsConsumer;
    }

    @Override
    public Consumer<String, EventSimilarityAvro> getEventsSimilarityConsumer() {
        if (eventsSimilarityConsumer == null) {
            eventsSimilarityConsumer = new KafkaConsumer<>(eventsSimilarity.consumer.getProperties());
            log.info("Создали косьюмер для events similarity с groupId = {}", eventsSimilarity.consumer.getProperties().get("group.id"));
        }
        return eventsSimilarityConsumer;
    }

    @Override
    public void stop() {
        if (userActionsConsumer != null) {
            log.info("Закрываем косьюмер для user actions");
            userActionsConsumer.close();
        }
        if (eventsSimilarityConsumer != null) {
            log.info("Закрываем консьюмер для events similarity");
            eventsSimilarityConsumer.close();
        }
    }

}
