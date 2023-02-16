package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

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

    @GetMapping
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film saveFilm(@RequestBody Film film) {
        log.info("Получен запрос к эндпоинту: 'POST/films', тело запроса: {}", film);
        validateFilm(film, Method.POST);

        final int id = generateId();
        film.setId(id);
        films.put(id, film);
        log.info("Фильм с id = " + id + " сохранен.");
        return film;
    }

    @PutMapping
    public Film putFilm(@RequestBody Film film) {
        log.info("Получен запрос к эндпоинту: 'PUT/films', тело запроса: {}", film);
        validateFilm(film, Method.PUT);
        films.put(film.getId(), film);
        log.info("Фильм с id = " + film.getId() + " обновлен.");
        return film;
    }

    private void validateFilm(Film film, Method method) {
        if (method.equals(Method.POST) && film.getId() != null && films.containsKey(film.getId())) {
            String message = "Фильм уже существует.";
            log.warn(message);
            throw new ValidationException(message);
        } else if (method.equals(Method.PUT) && (film.getId() == null || !films.containsKey(film.getId()))) {
            String message = "Фильма не существует.";
            log.warn(message);
            throw new ValidationException(message);
        }

        if (film.getName() == null || film.getName().isBlank()) {
            String message = String.format("Передано некорректное имя фильма: %s", film.getName());
            log.warn(message);
            throw new ValidationException(message);
        }

        if (film.getDescription().length() > 200) {
            String message = String.format("Описание длиннее 200 символов: %s", film.getName());
            log.warn(message);
            throw new ValidationException(message);
        }

        final LocalDate firstFilmDate = LocalDate.of(1895, 12, 28);
        if (firstFilmDate.isAfter(film.getReleaseDate())) {
            String message = "Дата выхода фильма не может быть позже дня рождения кино";
            log.warn(message);
            throw new ValidationException(message);
        }

        if (film.getDuration() < 0) {
            String message = String.format("Длительность фильма не может быть отрицательной %d"
                    , film.getDuration());
            log.warn(message);
            throw new ValidationException(message);
        }
    }

    private Integer generateId() {
        return nextId++;
    }

    private enum Method {GET, POST, PUT, DELETE}
}
