package ru.practicum.explorewithme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"ru.practicum.explorewithme", "ru.practicum.client"})
public class ExploreWithMeMainService {
    public static void main(String[] args) {
        SpringApplication.run(ExploreWithMeMainService.class, args);
    }
}
