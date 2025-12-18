package ru.practicum.explorewithme.compilation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CompilationApp {
    public static void main(String[] args) {
        SpringApplication.run(CompilationApp.class, args);
    }
}
