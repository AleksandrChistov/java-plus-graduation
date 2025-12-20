package ru.practicum.explorewithme.category;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CategoryApp {
    public static void main(String[] args) {
        SpringApplication.run(CategoryApp.class, args);
    }
}
