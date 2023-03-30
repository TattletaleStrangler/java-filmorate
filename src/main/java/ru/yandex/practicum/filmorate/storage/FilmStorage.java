package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film createFilm(Film film);

    Film getFilmById(Integer id);

    List<Film> getAll();

    void deleteFilmById(Integer filmId);

    Film updateFilm(Film film);
}
