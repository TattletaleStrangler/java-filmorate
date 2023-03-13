package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final String notFoundUserFormat = "Пользователь с id = %d не найден.";
    private final String notFoundFilmFormat = "Фильм с id = %d не найден.";

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public Film getFilmById(Integer filmId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            log.warn(String.format(notFoundFilmFormat, filmId));
            NotFoundException.throwException(notFoundFilmFormat, filmId);
        }
        return film;
    }

    public List<Film> getFilms() {
        return filmStorage.getAll();
    }

    public void deleteFilmById(Integer filmId) {
        filmStorage.deleteFilmById(filmId);
    }

    public Film updateFilm(Film film) {
        Integer filmId = film.getId();
        if (filmId == null || filmStorage.getFilmById(filmId) == null) {
            log.warn(String.format(notFoundFilmFormat, filmId));
            NotFoundException.throwException(notFoundFilmFormat, filmId);
        }
        return filmStorage.updateFilm(film);
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userService.getUserById(userId);

        if (film == null) {
            log.warn(String.format(notFoundFilmFormat, filmId));
            NotFoundException.throwException(notFoundFilmFormat, filmId);
        }
        if (user == null) {
            log.warn(String.format(notFoundUserFormat, userId));
            NotFoundException.throwException(notFoundUserFormat, userId);
        }

        film.addLike(userId);
        filmStorage.updateFilm(film);
    }

    public void removeLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilmById(filmId);

        if (film == null) {
            log.warn(String.format(notFoundFilmFormat, filmId));
            NotFoundException.throwException(notFoundFilmFormat, filmId);
        }
        if (film.getUsersLikes() == null || !film.getUsersLikes().contains(userId)) {
            log.warn(String.format(notFoundUserFormat, userId));
            NotFoundException.throwException(notFoundUserFormat, userId);
        }

        film.removeLike(userId);
        filmStorage.updateFilm(film);
    }

    public List<Film> getPopularFilms(Integer count) {
        List<Film> films = filmStorage.getAll();

        return films.stream()
                .sorted((f0, f1) -> f1.getUsersLikes().size() - f0.getUsersLikes().size())
                .limit(count)
                .collect(Collectors.toList());
    }
}
