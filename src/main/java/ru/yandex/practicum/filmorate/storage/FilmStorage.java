package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface FilmStorage {
    Film createFilm(Film film);

    Film getFilmById(Integer id);

    List<Film> getAll();

    void deleteFilmById(Integer filmId);

    Film updateFilm(Film film);
}
