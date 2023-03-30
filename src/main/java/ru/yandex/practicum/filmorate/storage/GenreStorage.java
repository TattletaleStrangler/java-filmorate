package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreStorage {
    Genre createGenre(Genre genre);

    Genre getGenreById(Integer id);

    List<Genre> getAll();

    List<Genre> getGenresByFilmId(Integer filmId);

    void deleteGenreById(Integer genreId);

    Genre updateGenre(Genre genre);
}
