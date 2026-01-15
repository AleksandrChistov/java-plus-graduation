package ru.practicum.stats.analyzer.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.analyzer.config.KafkaConfig;
import ru.practicum.stats.analyzer.service.EventsSimilarityService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class EventsSimilarityHandler {

    private final KafkaConfig kafka;

    private final EventsSimilarityService eventsSimilarityService;

    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    private final int BATCH_SIZE = 50;

    /**
     * Метод для начала процесса обработки событий схожести.
     * Подписывается на топики, получает события схожести и сохраняет их в БД.
     */
    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void saveEventsSimilarity() {
        final Consumer<String, EventSimilarityAvro> consumer = kafka.getEventsSimilarityConsumer();

        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Получен сигнал на завершение работы event similarity консьюмера");
                consumer.wakeup();
            }));

            consumer.subscribe(kafka.eventSimilarityTopics());

            while (true) {
                ConsumerRecords<String, EventSimilarityAvro> records = consumer.poll(kafka.eventSimilarityPollTimeout());
                int count = 0;

                for (ConsumerRecord<String, EventSimilarityAvro> record : records) {
                    handleRecord(record);
                    manageOffsets(consumer, record, count);
                    count++;
                }
                // фиксируем максимальный оффсет обработанных записей
                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {
            log.error("Получен WakeupException");
        } catch (Exception e) {
            log.error("Ошибка во время обработки коэффицентов сходства", e);
        } finally {
            try {
                log.debug("Фиксация смещений");
                consumer.commitSync();
            } finally {
                kafka.stop();
            }
        }
    }

    private void handleRecord(ConsumerRecord<String, EventSimilarityAvro> record) {
        EventSimilarityAvro event = record.value();

        log.info("Получено событие схожести: {}", event.toString());

        eventsSimilarityService.save(event);
    }

    private void manageOffsets(
            Consumer<String, EventSimilarityAvro> consumer,
            ConsumerRecord<String, EventSimilarityAvro> record,
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
}
