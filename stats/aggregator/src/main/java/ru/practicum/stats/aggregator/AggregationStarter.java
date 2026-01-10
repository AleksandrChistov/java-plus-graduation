package ru.practicum.stats.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.aggregator.config.KafkaConfig;
import ru.practicum.stats.aggregator.service.EventsSimilarityService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class AggregationStarter {

    private final KafkaConfig kafka;

    private final EventsSimilarityService eventsSimilarityService;

    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    private final int BATCH_SIZE = 50;

    /**
     * Метод для начала процесса агрегации данных.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        final Consumer<Long, UserActionAvro> consumer = kafka.userActionsConsumer();
        final Producer<String, EventSimilarityAvro> producer = kafka.eventSimilarityProducer();

        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Получен сигнал на завершение работы в {}", AggregationStarter.class.getSimpleName());
                consumer.wakeup();
            }));

            consumer.subscribe(kafka.userActionsTopics());

            while (true) {
                ConsumerRecords<Long, UserActionAvro> records = consumer.poll(kafka.userActionsPollTimeout());
                int count = 0;

                for (ConsumerRecord<Long, UserActionAvro> record : records) {
                    handleRecord(record, producer);
                    manageOffsets(consumer, record, count);
                    count++;
                }
                // фиксируем максимальный оффсет обработанных записей
                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {
            log.error("Получен WakeupException");
        } catch (Exception e) {
            log.error("Ошибка во время обработки действий пользователя", e);
        } finally {
            try {
                log.debug("Очистка буфера и фиксация смещений");
                producer.flush();
                consumer.commitSync();
            } finally {
                kafka.stop();
            }
        }
    }

    private void handleRecord(ConsumerRecord<Long, UserActionAvro> record, Producer<String, EventSimilarityAvro> producer) {
        UserActionAvro event = record.value();
        log.info("Получено событие от пользователя: {}", event.toString());

        List<EventSimilarityAvro> similarities = eventsSimilarityService.updateState(event);

        similarities.forEach(similarity -> {
            String key = getSimilarityKey(similarity.getEventA(), similarity.getEventB());

            kafka.eventSimilarityTopics().forEach(topic -> {
                ProducerRecord<String, EventSimilarityAvro> message = new ProducerRecord<>(
                        topic,
                        key,
                        similarity
                );

                log.debug("Отправляем сходство мероприятий в кафку: {}", similarity);

                producer.send(message, (metadata, exception) -> {
                    if (exception != null) {
                        log.error("Ошибка при отправке сообщения в Kafka, topic: {}, key: {}",
                                topic, key, exception);
                    } else {
                        log.info("Сообщение успешно отправлено в Kafka, topic: {}, partition: {}, offset: {}",
                                metadata.topic(), metadata.partition(), metadata.offset());
                    }
                });
            });
        });
    }

    private void manageOffsets(
            Consumer<Long, UserActionAvro> consumer,
            ConsumerRecord<Long, UserActionAvro> record,
            int count
    ) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        // at-least-once семантика
        if (count % BATCH_SIZE == 0) {
            log.debug("Асинхронно фиксируем смещения обработанных сообщений");
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка при фиксации смещений: {}", offsets, exception);
                } else {
                    log.debug("Успешно зафиксированы смещения: {}", offsets);
                }
            });
        }
    }

    private String getSimilarityKey(long eventA, long eventB) {
        return Math.min(eventA, eventB) + ":" + Math.max(eventA, eventB);
    }

}
