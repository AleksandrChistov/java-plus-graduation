package ru.practicum.stats.collector.kafka;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.practicum.stats.collector.config.KafkaConfig;

import java.time.Duration;

@Component
@Slf4j
public class KafkaUserActionProducer {

    private final KafkaProducer<Long, SpecificRecordBase> producer;

    public KafkaUserActionProducer(KafkaConfig kafkaConfig) {
        producer = new KafkaProducer<>(kafkaConfig.getUserActionsProperties());
    }

    public void send(String topic, Long eventTimestamp, Long key, SpecificRecordBase data) {
        ProducerRecord<Long, SpecificRecordBase> record = new ProducerRecord<>(topic, null, eventTimestamp, key, data);

        producer.send(record, (metadata, exception) -> {
            String eventName = data != null ? data.getClass().getSimpleName() : "null";
            if (exception == null) {
                log.info("Событие {} было успешно сохранено в топик {}, в партицию {}, со смещением {}, ключ '{}', timestamp {}",
                        eventName,
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset(),
                        key,
                        eventTimestamp
                );
            } else {
                log.error(
                        "Не удалось записать событие {} в топик {} (Ключ: {}). Ошибка: {}",
                        eventName,
                        topic,
                        key,
                        exception.getMessage(),
                        exception
                );
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down producer");
        producer.flush();
        producer.close(Duration.ofSeconds(10));
    }
}
