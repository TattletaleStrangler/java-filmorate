package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre_storage.GenreStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;
    private final String NOT_FOUND_GENRE_FORMAT = "Жанр с id = %d не найден.";

    public Genre getGenreById(Integer genreId) {
        Genre genre = genreStorage.getGenreById(genreId);
        if (genre == null) {
            log.warn(String.format(NOT_FOUND_GENRE_FORMAT, genreId));
            NotFoundException.throwException(NOT_FOUND_GENRE_FORMAT, genreId);
        }
        return genre;
    }

    public List<Genre> getGenres() {
        return genreStorage.getAll();
    }
}