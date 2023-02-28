package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validator.ValidateService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private Map<Integer, Film> films = new HashMap<>();
    private Integer nextId = 1;
    private ValidateService validateService;

    public FilmController(ValidateService validateService) {
        this.validateService = validateService;
    }

    @GetMapping
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film saveFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос к эндпоинту: 'POST/films', тело запроса: {}", film);

        if (film.getId() != null && films.containsKey(film.getId())) {
            String message = "Фильм c id = " + film.getId() + "уже существует.";
            log.warn(message);
            throw new AlreadyExistException(message);
        }

        validateService.validateFilm(film);
        final int id = generateId();
        film.setId(id);
        films.put(id, film);
        log.info("Фильм с id = " + id + " сохранен.");
        return film;
    }

    @PutMapping
    public Film putFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос к эндпоинту: 'PUT/films', тело запроса: {}", film);

        if (film.getId() == null || !films.containsKey(film.getId())) {
            String message = "Фильма не существует.";
            log.warn(message);
            throw new NotFoundException(message);
        }

        validateService.validateFilm(film);
        films.put(film.getId(), film);
        log.info("Фильм с id = " + film.getId() + " обновлен.");
        return film;
    }

    private Integer generateId() {
        return nextId++;
    }
}
