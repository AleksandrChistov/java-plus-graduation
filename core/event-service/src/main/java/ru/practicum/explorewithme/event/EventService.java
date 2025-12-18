package ru.practicum.explorewithme.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {
        "ru.practicum.explorewithme.event",
        "ru.practicum.client"
})
public class EventService {
    public static void main(String[] args) {
        SpringApplication.run(EventService.class, args);
    }
}
