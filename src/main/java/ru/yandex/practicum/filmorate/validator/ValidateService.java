package ru.yandex.practicum.filmorate.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Component
@Slf4j
public class ValidateService {

    public void validateUser(User user) {
    }

    public void validateFilm(Film film) {
        final LocalDate firstFilmDate = LocalDate.of(1895, 12, 28);
        if (firstFilmDate.isAfter(film.getReleaseDate())) {
            String message = "Дата выхода фильма не может быть позже дня рождения кино";
            log.warn(message);
            throw new ValidationException(message);
        }
    }
}
