package ru.practicum.explorewithme.event.client.request;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.explorewithme.api.request.service.AdminRequestServiceApi;
import ru.practicum.explorewithme.event.config.RequestClientConfig;

@FeignClient(name = "main-service", configuration = RequestClientConfig.class, fallback = RequestClientFallback.class)
public interface RequestClient extends AdminRequestServiceApi {
}
