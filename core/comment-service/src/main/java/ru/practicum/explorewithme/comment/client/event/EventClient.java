package ru.practicum.explorewithme.comment.client.event;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.explorewithme.api.event.service.EventServiceApi;
import ru.practicum.explorewithme.comment.config.EventClientConfig;

@FeignClient(name = "event-service", configuration = EventClientConfig.class, fallback = EventClientFallback.class)
public interface EventClient extends EventServiceApi {
}
