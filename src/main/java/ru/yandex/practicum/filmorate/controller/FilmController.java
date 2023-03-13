package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public List<Film> getFilms() {
        log.info("Получен запрос к эндпоинту: 'GET /films'");
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable(name = "id") Integer filmId) {
        log.info("Получен запрос к эндпоинту: 'GET /films/{}'", filmId);
        Film film = filmService.getFilmById(filmId);
        return film;
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(@RequestParam(name = "count", required = false, defaultValue = "10") Integer count) {
        log.info("Получен запрос к эндпоинту: 'GET /films/popular', count = {}", count);
        return filmService.getPopularFilms(count);
    }

    @PostMapping
    public Film saveFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос к эндпоинту: 'POST /films', тело запроса: {}", film);
        Film savedFilm = filmService.createFilm(film);
        log.info("Фильм с id = " + savedFilm.getId() + " сохранен.");
        return savedFilm;
    }

    @PutMapping
    public Film putFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос к эндпоинту: 'PUT/films', тело запроса: {}", film);
        Film updatedFilm = filmService.updateFilm(film);
        log.info("Фильм с id = " + film.getId() + " обновлен.");
        return updatedFilm;
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable Integer filmId) {
        log.info("Получен запрос к эндпоинту: 'DELETE /films/{}'", filmId);
        filmService.deleteFilmById(filmId);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable(name = "id") Integer filmId, @PathVariable(name = "userId") Integer userId) {
        log.info("Получен запрос к эндпоинту: 'PUT /films/{}/like/{}'", filmId, userId);
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable(name = "id") Integer filmId, @PathVariable(name = "userId") Integer userId) {
        log.info("Получен запрос к эндпоинту: 'DELETE /films/{}/like/{}'", filmId, userId);
        filmService.removeLike(filmId, userId);
    }
}
