package ru.practicum.stats.collector.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;
import ru.practicum.stats.collector.config.KafkaConfig;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Component
@Slf4j
public class KafkaUserActionProducer {

    private final KafkaProducer<String, SpecificRecordBase> producer;

    public KafkaUserActionProducer(KafkaConfig kafkaConfig) {
        Properties config = new Properties();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getServer());

        config.putAll(kafkaConfig.getUserActionsProperties());

        producer = new KafkaProducer<>(config);
    }

    public void send(String topic, Long eventTimestamp, String key, SpecificRecordBase data) {
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, null, eventTimestamp, key, data);
        Future<RecordMetadata> futureResult = producer.send(record);
        String eventName = data != null ? data.getClass().getSimpleName() : "null";
        try {
            RecordMetadata metadata = futureResult.get();
            log.info("Событие {} было успешно сохранено в топик {}, в партицию {}, со смещением {}, ключ '{}', timestamp {}",
                    eventName,
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset(),
                    key,
                    eventTimestamp
            );
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Не удалось записать событие {} в топик {}", eventName, topic, e);
        } finally {
            producer.flush();
            producer.close(Duration.ofSeconds(10));
        }
    }
}
