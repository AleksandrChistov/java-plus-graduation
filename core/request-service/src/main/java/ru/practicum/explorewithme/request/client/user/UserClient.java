package ru.practicum.explorewithme.request.client.user;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.explorewithme.api.user.service.UserServiceApi;
import ru.practicum.explorewithme.request.config.UserClientConfig;

@FeignClient(name = "user-service", configuration = UserClientConfig.class, fallback = UserClientFallback.class)
public interface UserClient extends UserServiceApi {
}
