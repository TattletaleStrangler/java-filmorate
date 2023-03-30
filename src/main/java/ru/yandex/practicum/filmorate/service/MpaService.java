package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa_storage.MpaRatingStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MpaService {
    private final MpaRatingStorage mpaRatingStorage;
    private final String NOT_FOUND_MPA_FORMAT = "MPA рейтинг с id = %d не найден.";

    public MpaRating getMpaById(Integer mpaId) {
        MpaRating mpaRating = mpaRatingStorage.getMpaRatingById(mpaId);
        if (mpaRating == null) {
            log.warn(String.format(NOT_FOUND_MPA_FORMAT, mpaId));
            NotFoundException.throwException(NOT_FOUND_MPA_FORMAT, mpaId);
        }
        return mpaRating;
    }

    public List<MpaRating> getMpa() {
        return mpaRatingStorage.getAll();
    }

}