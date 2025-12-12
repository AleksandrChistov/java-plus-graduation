package ru.practicum.explorewithme.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.api.user.dto.UserDto;
import ru.practicum.explorewithme.api.user.service.UserServiceApi;
import ru.practicum.explorewithme.user.service.PrivateUserService;

@RestController
@RequiredArgsConstructor
public class PrivateUserController implements UserServiceApi {

    private final PrivateUserService userService;

    @Override
    public UserDto getUserById(long userId) {
        return userService.getUserById(userId);
    }
}
