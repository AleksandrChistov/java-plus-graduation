package ru.practicum.explorewithme.comment.client.user;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.explorewithme.api.user.service.UserServiceApi;
import ru.practicum.explorewithme.comment.config.UserClientConfig;

@FeignClient(name = "user-service", configuration = UserClientConfig.class, fallback = UserClientFallback.class)
public interface UserClient extends UserServiceApi {
}
