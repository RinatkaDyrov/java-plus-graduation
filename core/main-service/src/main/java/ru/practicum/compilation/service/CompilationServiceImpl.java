package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.CompilationRequestDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exeption.NotFoundException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CompilationDto> getAll(CompilationRequestDto requestDto) {
        List<Compilation> compilations = compilationRepository
                .findAllWithFilter(requestDto.getPinned(), requestDto.getFrom(), requestDto.getSize());

        return compilations.stream()
                .map(CompilationMapper::mapToDto)
                .toList();
    }

    @Override
    public CompilationDto getById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка", compId));

        return CompilationMapper.mapToDto(compilation);
    }

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto dto) {
        Compilation newCompilation = CompilationMapper.mapToCompilation(dto);
        newCompilation.setEvents(getEventsByIds(dto.getEvents()));

        return CompilationMapper.mapToDto(compilationRepository.save(newCompilation));
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Подборка", compId);
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка", compId));
        updateFields(compilation, request);

        return CompilationMapper.mapToDto(compilationRepository.save(compilation));
    }

    private List<Event> getEventsByIds(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }

        List<Event> compEvents = eventRepository.findAllById(ids);

        if (compEvents.size() != ids.size()) {
            Set<Long> foundedIds = compEvents.stream().map(Event::getId).collect(Collectors.toSet());
            List<Long> notFound = ids.stream()
                    .filter(id -> !foundedIds.contains(id))
                    .toList();
            log.warn("События с ID не найдены: {}", notFound);
        }

        return compEvents;
    }

    private void updateFields(Compilation compilation, UpdateCompilationRequest request) {
        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }
        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }
        if (request.getEvents() != null) {
            compilation.setEvents(getEventsByIds(request.getEvents()));
        }
    }
}
