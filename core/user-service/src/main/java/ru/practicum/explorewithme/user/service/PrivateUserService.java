package ru.practicum.explorewithme.user.service;

import ru.practicum.explorewithme.api.user.dto.UserDto;
import ru.practicum.explorewithme.api.user.service.UserServiceApi;

public interface PrivateUserService extends UserServiceApi {

    UserDto getUserById(long userId);

}
