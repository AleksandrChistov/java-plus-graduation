package ru.practicum.explorewithme.event.client.request;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.explorewithme.api.request.service.RequestServiceApi;
import ru.practicum.explorewithme.event.config.RequestClientConfig;

@FeignClient(name = "request-service", configuration = RequestClientConfig.class, fallback = RequestClientFallback.class)
public interface RequestClient extends RequestServiceApi {
}
