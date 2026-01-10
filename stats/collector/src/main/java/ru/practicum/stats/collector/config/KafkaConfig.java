package ru.practicum.stats.collector.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Properties;

@Configuration
@ConfigurationProperties(prefix = "stats.collector.kafka")
@Setter
public class KafkaConfig {
    private UserActions userActions;

    @Getter
    @Setter
    public static class UserActions {
        private Properties properties;
        private List<String> topics;
    }

    public List<String> getUserActionsTopics() {
        return userActions.getTopics();
    }

    public Properties getUserActionsProperties() {
        return userActions.getProperties();
    }

}
