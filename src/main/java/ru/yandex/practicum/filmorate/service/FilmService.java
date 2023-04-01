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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.util.Constants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreService genreService;
    private final MpaService mpaService;

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

        setNameForMpa(film);
        setNameForGenres(film);
        return film;
    }

    public List<Film> getFilms() {
        List<Film> films = filmStorage.getAll();
        setNameForMpa(films);
        setNameForGenres(films);
        return films;
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
        Film updatedFilm = filmStorage.updateFilm(film);
        setNameForMpa(updatedFilm);
        setNameForGenres(updatedFilm);
        return updatedFilm;
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
        List<Film> popular = films.stream()
                .sorted((f0, f1) -> f1.getUsersLikes().size() - f0.getUsersLikes().size())
                .limit(count)
                .collect(Collectors.toList());
        setNameForMpa(popular);
        setNameForGenres(popular);
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

    private void setNameForGenres(Film film) {
        setNameForGenres(new ArrayList<>() {{
                add(film);
            }
        });
    }

    private void setNameForGenres(List<Film> films) {
        List<Genre> genresWithNames = genreService.getGenres();
        for (Film film : films) {
            List<Genre> genresWithoutNames = film.getGenres();
            film.setGenres(genresWithoutNames.stream()
                    .map(genresWithNames::indexOf)
                    .map(genresWithNames::get)
                    .collect(Collectors.toList()));
        }
    }

    private void setNameForMpa(Film film) {
        setNameForMpa(new ArrayList<>() {{
                add(film);
            }
        });
    }

    private void setNameForMpa(List<Film> films) {
        List<MpaRating> mpaRatingsWithNames = mpaService.getMpa();

        for (Film film : films) {
            if (film.getMpa() != null) {
                int index = mpaRatingsWithNames.indexOf(film.getMpa());
                MpaRating mpaWithName = mpaRatingsWithNames.get(index);
                film.setMpa(mpaWithName);
            }
        }
    }
}
