package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film_storage.FilmStorage;

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
    private final String notFoundUserFormat = "Пользователь с id = %d не найден.";
    private final String notFoundFilmFormat = "Фильм с id = %d не найден.";
    private final String notFoundGenreFormat = "Жанр с id = %d не найден.";
    private final String notFoundMpaFormat = "MPA рейтинг с id = %d не найден.";

    public Film createFilm(Film film) {
        checkFilmsData(film);
        return filmStorage.createFilm(film);
    }

    public Film getFilmById(Integer filmId) {
        Film film = filmStorage.getFilmById(filmId);

        if (film == null) {
            log.warn(String.format(notFoundFilmFormat, filmId));
            NotFoundException.throwException(notFoundFilmFormat, filmId);
        }

        setNameForMpa(film);
        setNameForGenres(film);
        return film;
    }

    public List<Film> getFilms() {
        List<Film> films = filmStorage.getAll();
        films.forEach(this::setNameForMpa);
        films.forEach(this::setNameForGenres);
        return films;
    }

    public void deleteFilmById(Integer filmId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            log.warn(String.format(notFoundFilmFormat, filmId));
            NotFoundException.throwException(notFoundFilmFormat, filmId);
        }
        filmStorage.deleteFilmById(filmId);
    }

    public Film updateFilm(Film film) {
        Integer filmId = film.getId();
        if (filmId == null || filmStorage.getFilmById(filmId) == null) {
            log.warn(String.format(notFoundFilmFormat, filmId));
            NotFoundException.throwException(notFoundFilmFormat, filmId);
        }
        checkFilmsData(film);
        Film updatedFilm = filmStorage.updateFilm(film);
        setNameForMpa(updatedFilm);
        setNameForGenres(updatedFilm);
        return updatedFilm;
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
        List<Film> popular = films.stream()
                .sorted((f0, f1) -> f1.getUsersLikes().size() - f0.getUsersLikes().size())
                .limit(count)
                .collect(Collectors.toList());
        popular.forEach(this::setNameForMpa);
        popular.forEach(this::setNameForGenres);
        return popular;
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
            NotFoundException.throwException(notFoundGenreFormat, genre.getId());
        }
    }

    private void checkMpa(MpaRating mpaRating) {
        MpaRating mpaRatingFromDb = mpaService.getMpaById(mpaRating.getId());
        if (mpaRatingFromDb == null) {
            NotFoundException.throwException(notFoundMpaFormat, mpaRating.getId());
        }
    }

    private void checkUser(Integer userId) {
        User userFromDb = userService.getUserById(userId);
        if (userFromDb == null) {
            NotFoundException.throwException(notFoundUserFormat, userId);
        }
    }

    private void setNameForGenres(Film film) {
        film.getGenres().forEach((genre) -> genre.setName(genreService.getGenreById(genre.getId()).getName()));
    }

    private void setNameForMpa(Film film) {
        film.setMpa(mpaService.getMpaById(film.getMpa().getId()));
    }
}
