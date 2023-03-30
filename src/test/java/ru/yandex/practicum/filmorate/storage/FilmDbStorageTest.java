package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film_storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre_storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa_storage.MpaRatingDbStorage;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final MpaRatingDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;
    public Film film1;
    public Film film2;
    public Film film3;

    @BeforeEach
    public void beforeEach() {
        film1 = new Film();
        film2 = new Film();
        film3 = new Film();

        List<Film> films = Arrays.asList(film1, film2, film3);

        final int[] ordinal = new int[]{1};

        films.forEach(film -> {
            film.setName("name" + ordinal[0]);
            film.setDescription("Description" + ordinal[0]);
            film.setDuration(100 + ordinal[0] * 5);
            film.setReleaseDate(LocalDate.of(1900 + ordinal[0] * 10, 1 + ordinal[0]
                    , 1 + ordinal[0] * 2));
            film.setMpa(mpaStorage.getMpaRatingById(ordinal[0]));
            film.setGenres(genreStorage.getGenresByFilmId(ordinal[0]));
            ordinal[0]++;
        });
    }

    @Test
    void testCreateFilm() {
        filmStorage.createFilm(film1);

        Optional<Film> filmOptional = Optional.ofNullable(filmStorage.getFilmById(1));

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", 1)
                );
    }

    @Test
    void testGetFilmById() {
        filmStorage.createFilm(film1);

        Optional<Film> filmOptional = Optional.ofNullable(filmStorage.getFilmById(1));

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film).hasFieldOrPropertyWithValue("id", 1);
                    assertThat(film).hasFieldOrPropertyWithValue("name", "name1");
                    assertThat(film).hasFieldOrPropertyWithValue("description", "Description1");
                    assertThat(film).hasFieldOrPropertyWithValue("duration", 105);
                    assertThat(film).hasFieldOrPropertyWithValue("mpa", mpaStorage.getMpaRatingById(1));
                });
    }

    @Test
    void testGetAll() {
        filmStorage.createFilm(film1);
        filmStorage.createFilm(film2);
        filmStorage.createFilm(film3);

        film1.setId(1);
        film2.setId(2);
        film3.setId(3);

        List<Film> expectedFilms = List.of(film1, film2, film3);
        assertIterableEquals(expectedFilms, filmStorage.getAll(), "Списки фильмов не совпадают");
    }

    @Test
    void testDeleteFilmById() {
        Film film = filmStorage.createFilm(film1);

        filmStorage.deleteFilmById(film.getId());

        Optional<Film> filmOptional = Optional.ofNullable(filmStorage.getFilmById(film.getId()));

        assertFalse(filmOptional.isPresent());
    }

    @Test
    void testUpdateFilm() {
        filmStorage.createFilm(film1);

        String updatedName = "Updated Name";
        String updatedDescription = "Updated Description";
        Integer updatedDuration = 300;
        MpaRating updatedMpa = mpaStorage.getMpaRatingById(2);

        film1.setName(updatedName);
        film1.setDescription("Updated Description");
        film1.setDuration(updatedDuration);
        film1.setMpa(updatedMpa);

        filmStorage.updateFilm(film1);

        Optional<Film> filmOptional = Optional.ofNullable(filmStorage.getFilmById(1));

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film).hasFieldOrPropertyWithValue("id", 1);
                    assertThat(film).hasFieldOrPropertyWithValue("name", updatedName);
                    assertThat(film).hasFieldOrPropertyWithValue("description", updatedDescription);
                    assertThat(film).hasFieldOrPropertyWithValue("duration", updatedDuration);
                    assertThat(film).hasFieldOrPropertyWithValue("mpa", updatedMpa);
                });
    }
}