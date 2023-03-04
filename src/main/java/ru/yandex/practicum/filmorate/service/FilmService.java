package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.service.FilmService.Model.*;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;


    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    private void throwNotFoundException(Model model, Integer id) {
        String message = "";
        if (model.equals(FILM)) {
            message = String.format("Фильм c id = %d не найден", id);
        } else if (model.equals(Model.USER)) {
            message = String.format("Пользователь c id = %d не найден", id);
        }

        log.warn(message);
        throw new NotFoundException(message);
    }

    enum Model {FILM, USER}

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public Film getFilmById(Integer filmId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            throwNotFoundException(FILM, filmId);
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
            throwNotFoundException(FILM, filmId);
        }
        return filmStorage.updateFilm(film);
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userService.getUserById(userId);

        if (film == null) {
            throwNotFoundException(FILM, filmId);
        }
        if (user == null) {
            throwNotFoundException(USER, userId);
        }

        film.addLike(userId);
        filmStorage.updateFilm(film);
    }

    public void removeLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilmById(filmId);

        if (film == null) {
            throwNotFoundException(FILM, filmId);
        }
        if (film.getUsersLikes() == null || !film.getUsersLikes().contains(userId)){
            throwNotFoundException(USER, filmId);
        }

        film.removeLike(userId);
        filmStorage.updateFilm(film);
    }

    public List<Film> getPopularFilms(Integer count) {
        return filmStorage.getAll().stream()
                .sorted((f0, f1) -> f1.getUsersLikes().size() - f0.getUsersLikes().size())
                .limit(count)
                .collect(Collectors.toList());
    }
}
