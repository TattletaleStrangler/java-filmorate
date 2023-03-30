package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

public interface MpaRatingStorage {
    MpaRating createMpaRating(MpaRating mpaRating);

    MpaRating getMpaRatingById(Integer id);

    List<MpaRating> getAll();

    void deleteMpaRatingById(Integer mpaRatingId);

    MpaRating updateMpaRating(MpaRating mpaRating);
}
