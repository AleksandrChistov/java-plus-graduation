package ru.practicum.explorewithme.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.api.user.dto.UserDto;
import ru.practicum.explorewithme.api.user.service.UserServiceApi;
import ru.practicum.explorewithme.user.service.PrivateUserService;

import java.util.List;
import java.util.Set;

@RestController
@Validated
@RequiredArgsConstructor
public class PrivateUserController implements UserServiceApi {

    private final PrivateUserService userService;

    @Override
    public UserDto getUserById(Long userId) {
        return userService.getUserById(userId);
    }

    @Override
    public List<UserDto> getAllByIds(Set<Long> userIds) {
        return userService.getAllByIds(userIds);
    }
}
