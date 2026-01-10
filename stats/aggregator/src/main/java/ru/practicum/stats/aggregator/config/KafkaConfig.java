package ru.practicum.stats.aggregator.config;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.List;

public interface KafkaConfig {

    Duration userActionsPollTimeout();

    List<String> userActionsTopics();

    List<String> eventSimilarityTopics();

    Consumer<Long, UserActionAvro> userActionsConsumer();

    Producer<String, EventSimilarityAvro> eventSimilarityProducer();

    void stop();

}
