package ru.practicum.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.StatsDto;
import ru.practicum.StatsParams;
import ru.practicum.StatsView;
import ru.practicum.client.config.StatsService;
import ru.practicum.client.exception.StatsClientException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class StatsClientImpl implements StatsClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private final StatsService statsService;

    public StatsClientImpl(ObjectMapper objectMapper, StatsService statsService) {
        this.objectMapper = objectMapper;
        this.statsService = statsService;
    }

    @Override
    public void hit(StatsDto statsDto) {
        try {
            String requestBody = objectMapper.writeValueAsString(statsDto);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(statsService.makeUri("/hit"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

            if (response.statusCode() >= 400) {
                throw new StatsClientException("Failed to send hit: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new StatsClientException("Failed to send hit", e);
        }
    }

    @Override
    public List<StatsView> getStats(StatsParams statsParams) {
        try {
            String uriStr = statsService.makeUri("/stats").toString();
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(uriStr)
                    .queryParam("start", encodeDateTime(statsParams.getStart()))
                    .queryParam("end", encodeDateTime(statsParams.getEnd()));

            if (statsParams.getUris() != null && !statsParams.getUris().isEmpty()) {
                statsParams.getUris().forEach(uri -> uriBuilder.queryParam("uris", uri));
            }

            if (statsParams.getUnique() != null) {
                uriBuilder.queryParam("unique", statsParams.getUnique());
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uriBuilder.toUriString()))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new StatsClientException("Failed to get stats: " + response.statusCode());
            }

            return objectMapper.readValue(
                    response.body(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, StatsView.class)
            );
        } catch (Exception e) {
            throw new StatsClientException("Failed to get stats", e);
        }
    }

    private String encodeDateTime(LocalDateTime dateTime) {
        return dateTime.format(FORMATTER);
    }
}
