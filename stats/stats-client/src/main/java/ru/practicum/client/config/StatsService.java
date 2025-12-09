package ru.practicum.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.client.exception.StatsServerUnavailable;

import java.net.URI;

@Component
public class StatsService {

    private final DiscoveryClient discoveryClient;

    private final String statsServiceId;

    private final RetryTemplate retryTemplate = RetryTemplate.builder()
            .maxAttempts(3)
            .exponentialBackoff(100, 5, 2000)
            .retryOn(StatsServerUnavailable.class)
            .build();

    private StatsService(@Value("${service.ids.stats}") String statsServiceId, DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
        this.statsServiceId = statsServiceId;
    }

    public URI makeUri(String path) {
        ServiceInstance instance = retryTemplate.execute(cxt -> getInstance());
        return URI.create("http://" + instance.getHost() + ":" + instance.getPort() + path);
    }

    public URI makeUri() {
        return this.makeUri("");
    }

    private ServiceInstance getInstance() {
        try {
            return discoveryClient
                    .getInstances(statsServiceId)
                    .getFirst();
        } catch (Exception exception) {
            throw new StatsServerUnavailable(
                    "Ошибка обнаружения адреса сервиса статистики с id: " + statsServiceId,
                    exception
            );
        }
    }
}
