package ru.practicum.stats.analyzer.config;

import org.apache.kafka.clients.consumer.Consumer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.List;

public interface KafkaConfig {

    Duration userActionsPollTimeout();

    Duration eventSimilarityPollTimeout();

    List<String> userActionsTopics();

    List<String> eventSimilarityTopics();

    Consumer<Long, UserActionAvro> getUserActionsConsumer();

    Consumer<String, EventSimilarityAvro> getEventsSimilarityConsumer();

    void stop();

}
