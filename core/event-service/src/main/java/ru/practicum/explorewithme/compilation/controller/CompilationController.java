package ru.practicum.explorewithme.compilation.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.compilation.dto.ResponseCompilationDto;
import ru.practicum.explorewithme.compilation.service.CompilationService;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(path = CompilationController.URL, produces = MediaType.APPLICATION_JSON_VALUE)
public class CompilationController {

    public static final String URL = "/compilations";

    private final CompilationService compilationService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ResponseCompilationDto> getAll(
            @RequestParam(required = false) Boolean pinned,
            @PositiveOrZero @RequestParam(defaultValue = "0") int from,
            @Positive @RequestParam(defaultValue = "10") int size
    ) {
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseCompilationDto getById(@Positive @PathVariable Long compId) {
        return compilationService.getCompilation(compId);
    }

}
