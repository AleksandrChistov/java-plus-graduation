package ru.practicum.explorewithme.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.StatsDto;
import ru.practicum.StatsParams;
import ru.practicum.StatsView;
import ru.practicum.explorewithme.service.StatsService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    void saveHit(@Valid @RequestBody StatsDto statsDto) {
        log.info("Поступил запрос post/hit на создание hit {}", statsDto);
        statsService.saveHit(statsDto);
        log.info("Запрос post/hit успешно обработан.");
    }

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    List<StatsView> getStat(@Valid @ModelAttribute StatsParams params) {
        log.info("Поступил запрос get/stats на получение статистики {}", params);
        List<StatsView> viewStats = statsService.getStats(params);
        log.info("Запрос get/stats успешно обработан. Список статистики: {}", viewStats);
        return viewStats;
    }
}
