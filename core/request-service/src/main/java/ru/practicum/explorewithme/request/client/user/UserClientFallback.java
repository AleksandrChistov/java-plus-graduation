package ru.practicum.explorewithme.request.client.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.api.user.dto.UserDto;
import ru.practicum.explorewithme.api.user.service.UserServiceApi;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class UserClientFallback implements UserServiceApi {

    @Override
    public UserDto getUserById(Long userId) {
        log.warn("Сервис User недоступен, fallback вернул null для id: {}", userId);
        return null;
    }

    @Override
    public List<UserDto> getAllByIds(Set<Long> userIds) {
        log.warn("Сервис User недоступен, fallback вернул пустой список для ids: {}", userIds);
        return List.of();
    }
}
