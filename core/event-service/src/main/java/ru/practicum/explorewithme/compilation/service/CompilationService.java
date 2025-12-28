package ru.practicum.explorewithme.compilation.service;

import ru.practicum.explorewithme.compilation.dto.CreateCompilationDto;
import ru.practicum.explorewithme.compilation.dto.ResponseCompilationDto;
import ru.practicum.explorewithme.compilation.dto.UpdateCompilationDto;

import java.util.List;

public interface CompilationService {

    List<ResponseCompilationDto> getCompilations(Boolean pinned, int from, int size);

    ResponseCompilationDto getCompilation(long compId);

    ResponseCompilationDto save(CreateCompilationDto requestCompilationDto);

    ResponseCompilationDto update(long compId, UpdateCompilationDto updateCompilationDto);

    void delete(long compId);

}
