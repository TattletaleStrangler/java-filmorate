package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@Slf4j
@RequiredArgsConstructor
public class MpaController {
    private final MpaService mpaService;

    @GetMapping
    public List<MpaRating> getMpa() {
        log.info("Получен запрос к эндпоинту: 'GET /map'");
        return mpaService.getMpa();
    }

    @GetMapping("/{id}")
    public MpaRating getMpaById(@PathVariable(name = "id") Integer mpaId) {
        log.info("Получен запрос к эндпоинту: 'GET /mpa/{id}'", mpaId);
        return mpaService.getMpaById(mpaId);
    }
}
