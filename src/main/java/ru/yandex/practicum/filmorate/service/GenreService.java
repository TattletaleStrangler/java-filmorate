package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre_storage.GenreStorage;

import static ru.yandex.practicum.filmorate.util.Constants.*;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;

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
