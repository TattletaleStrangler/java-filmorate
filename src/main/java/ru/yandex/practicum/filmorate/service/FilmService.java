package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
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
    private final GenreService genreService;
    private final MpaService mpaService;
    private final String NOT_FOUND_USER_FORMAT = "Пользователь с id = %d не найден.";
    private final String NOT_FOUND_FILM_FORMAT = "Фильм с id = %d не найден.";
    private final String NOT_FOUND_GENRE_FORMAT = "Жанр с id = %d не найден.";
    private final String NOT_FOUND_MPA_FORMAT = "MPA рейтинг с id = %d не найден.";

    public Film createFilm(Film film) {
        checkFilmsData(film);
        return filmStorage.createFilm(film);
    }

    public Film getFilmById(Integer filmId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            log.warn(String.format(NOT_FOUND_FILM_FORMAT, filmId));
            NotFoundException.throwException(NOT_FOUND_FILM_FORMAT, filmId);
        }
        return film;
    }

    public List<Film> getFilms() {
        return filmStorage.getAll();
    }

    public void deleteFilmById(Integer filmId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            log.warn(String.format(NOT_FOUND_FILM_FORMAT, filmId));
            NotFoundException.throwException(NOT_FOUND_FILM_FORMAT, filmId);
        }
        filmStorage.deleteFilmById(filmId);
    }

    public Film updateFilm(Film film) {
        Integer filmId = film.getId();
        if (filmId == null || filmStorage.getFilmById(filmId) == null) {
            log.warn(String.format(NOT_FOUND_FILM_FORMAT, filmId));
            NotFoundException.throwException(NOT_FOUND_FILM_FORMAT, filmId);
        }
        checkFilmsData(film);
        return filmStorage.updateFilm(film);
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userService.getUserById(userId);

        if (film == null) {
            log.warn(String.format(NOT_FOUND_FILM_FORMAT, filmId));
            NotFoundException.throwException(NOT_FOUND_FILM_FORMAT, filmId);
        }
        if (user == null) {
            log.warn(String.format(NOT_FOUND_USER_FORMAT, userId));
            NotFoundException.throwException(NOT_FOUND_USER_FORMAT, userId);
        }

        film.addLike(userId);
        filmStorage.updateFilm(film);
    }

    public void removeLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilmById(filmId);

        if (film == null) {
            log.warn(String.format(NOT_FOUND_FILM_FORMAT, filmId));
            NotFoundException.throwException(NOT_FOUND_FILM_FORMAT, filmId);
        }
        if (film.getUsersLikes() == null || !film.getUsersLikes().contains(userId)) {
            log.warn(String.format(NOT_FOUND_USER_FORMAT, userId));
            NotFoundException.throwException(NOT_FOUND_USER_FORMAT, userId);
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

    private void checkFilmsData(Film film) {
        List<Genre> genres = film.getGenres();
        if (genres != null) {
            genres.forEach(this::checkGenres);
        }

        checkMpa(film.getMpa());

        if (film.getUsersLikes() != null) {
            film.getUsersLikes().forEach(this::checkUser);
        }
    }

    private void checkGenres(Genre genre) {
        Genre genreFromDb = genreService.getGenreById(genre.getId());
        if (genreFromDb == null) {
            NotFoundException.throwException(NOT_FOUND_GENRE_FORMAT, genre.getId());
        }
    }

    private void checkMpa(MpaRating mpaRating) {
        MpaRating mpaRatingFromDb = mpaService.getMpaById(mpaRating.getId());
        if (mpaRatingFromDb == null) {
            NotFoundException.throwException(NOT_FOUND_MPA_FORMAT, mpaRating.getId());
        }
    }

    private void checkUser(Integer userId) {
        User userFromDb = userService.getUserById(userId);
        if (userFromDb == null) {
            NotFoundException.throwException(NOT_FOUND_USER_FORMAT, userId);
        }
    }
}
