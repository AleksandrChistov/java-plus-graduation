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
    @Getter
    private String server;
    @Getter
    private List<String> topics;
    private UserActions userActions;

    @Getter
    @Setter
    public static class UserActions {
        @Getter
        private Properties properties;
    }

    public Properties getUserActionsProperties() {
        return userActions.getProperties();
    }

}
