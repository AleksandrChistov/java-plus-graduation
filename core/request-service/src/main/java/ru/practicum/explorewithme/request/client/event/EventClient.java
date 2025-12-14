package ru.practicum.explorewithme.request.client.event;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.explorewithme.api.event.service.EventServiceApi;

@FeignClient(name = "event-service", configuration = EventClientErrorDecoder.class, fallback = EventClientFallback.class)
public interface EventClient extends EventServiceApi {
}
