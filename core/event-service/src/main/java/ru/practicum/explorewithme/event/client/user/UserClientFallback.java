package ru.practicum.explorewithme.event.client.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.api.user.dto.UserDto;
import ru.practicum.explorewithme.shared.error.exception.ServiceUnavailableException;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class UserClientFallback implements UserClient {

    @Override
    public UserDto getUserById(Long userId) {
        log.warn("Сервис User недоступен, fallback кинул ServiceUnavailableException для id: {}", userId);
        throw new ServiceUnavailableException("Сервис User недоступен");
    }

    @Override
    public List<UserDto> getAllByIds(Set<Long> userIds) {
        log.warn("Сервис User недоступен, fallback вернул пустой список для ids: {}", userIds);
        return List.of();
    }
}
