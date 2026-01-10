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
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.analyzer.config.KafkaConfig;
import ru.practicum.stats.analyzer.service.InteractionService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserInteractionHandler {

    private final KafkaConfig kafka;

    private final InteractionService interactionService;

    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    private final int BATCH_SIZE = 50;

    /**
     * Метод для начала процесса обработки действий пользователей.
     * Подписывается на топики, получает события действий пользователей и сохраняет их в БД.
     */
    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void saveUserActions() {
        final Consumer<Long, UserActionAvro> consumer = kafka.getUserActionsConsumer();

        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Получен сигнал на завершение работы user actions консьюмера");
                consumer.wakeup();
            }));

            consumer.subscribe(kafka.userActionsTopics());

            while (true) {
                ConsumerRecords<Long, UserActionAvro> records = consumer.poll(kafka.userActionsPollTimeout());
                int count = 0;

                for (ConsumerRecord<Long, UserActionAvro> record : records) {
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

    private void handleRecord(ConsumerRecord<Long, UserActionAvro> record) {
        UserActionAvro event = record.value();

        log.info("Получено событие действия пользователя: {}", event.toString());

        interactionService.save(event);
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
}
