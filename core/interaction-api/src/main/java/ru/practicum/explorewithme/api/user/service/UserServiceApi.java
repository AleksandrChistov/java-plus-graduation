package ru.practicum.explorewithme.api.user.service;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.explorewithme.api.user.dto.UserDto;

public interface UserServiceApi {
    String URL = "/api/v1/internal/users";

    @GetMapping(path = URL + "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    UserDto getUserById(@PathVariable long userId);

}
