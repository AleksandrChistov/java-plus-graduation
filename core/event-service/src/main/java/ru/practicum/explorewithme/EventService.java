package ru.practicum.explorewithme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "ru.practicum.explorewithme.event",
        "ru.practicum.explorewithme.compilation",
        "ru.practicum.explorewithme.shared",
        "ru.practicum.client"
})
@EnableFeignClients
public class EventService {
    public static void main(String[] args) {
        SpringApplication.run(EventService.class, args);
    }
}
