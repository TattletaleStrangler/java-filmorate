package ru.yandex.practicum.filmorate.storage.film_storage;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Primary
@Component
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film createFilm(Film film) {
        String sqlQueryInsertFilm = "insert into film(name, description, releaseDate, duration, mpa_rating_id) " +
                " values(?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQueryInsertFilm, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                stmt.setInt(5, film.getMpa().getId());
            } else {
                stmt.setInt(5, Types.INTEGER);
            }
            return stmt;
        }, keyHolder);
        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());

        String sqlQueryInsertGenres = "insert into film_genres(film_id, genre_id) values(?, ?)";

        if (film.getGenres() != null) {
            film.getGenres()
                    .stream()
                    .map(Genre::getId)
                    .forEach((genreId) -> {
                        jdbcTemplate.update(sqlQueryInsertGenres, film.getId(), genreId);
                    });
        }

        return film;
    }

    @Override
    public Film getFilmById(Integer id) {
        try {
            return findFilmById(id);
        } catch (DataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Film> getAll() {
        String sqlQuery = "select * from film";
        List<Film> films = jdbcTemplate.query(sqlQuery, this::mapRowToFilm);

        String sqlQueryGenres = "select genre_id from film_genres where film_id = ?";
        for (Film film : films) {
            List<Integer> genresId = jdbcTemplate.queryForList(sqlQueryGenres, Integer.class, film.getId());
            List<Genre> genres = genresId.stream()
                    .map((genreId) -> {
                        Genre genre = new Genre();
                        genre.setId(genreId);
                        return genre;
                    })
                    .collect(Collectors.toList());
            film.setGenres(genres);
        }

        String sqlQueryLikes = "select user_id from user_likes where film_id = ?";
        for (Film film : films) {
            List<Integer> userLikes = jdbcTemplate.queryForList(sqlQueryLikes, Integer.class, film.getId());
            userLikes.forEach(film::addLike);
        }

        return films;
    }

    @Override
    public void deleteFilmById(Integer filmId) {
        String sqlQuery = "delete from film where film_id = ?";
        jdbcTemplate.update(sqlQuery, filmId);
    }

    @Override
    public Film updateFilm(Film film) {
        try {
            updateGenres(film);
            updateUserLikes(film);
            updateOnlyFilm(film);

            return findFilmById(film.getId());
        } catch (DataAccessException e) {
            return null;
        }
    }

    private Film findFilmById(Integer id) {
        String sqlQuery = "select * from film where film_id = ?";
        Film film = jdbcTemplate.queryForObject(sqlQuery, this::mapRowToFilm, id);

        String sqlQueryGenres = "select genre_id from film_genres where film_id = ?";
        List<Integer> genresId = jdbcTemplate.queryForList(sqlQueryGenres, Integer.class, id);
        film.setGenres(genresId.stream()
                .map((genreId) -> {
                    Genre genre = new Genre();
                    genre.setId(genreId);
                    return genre;
                })
                .collect(Collectors.toList())
        );

        String sqlQueryLikes = "select user_id from user_likes where film_id = ?";
        List<Integer> userLikes = jdbcTemplate.queryForList(sqlQueryLikes, Integer.class, id);
        userLikes.forEach(film::addLike);

        return film;
    }

    private Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        Integer ratingId = resultSet.getInt("mpa_rating_id");
        MpaRating rating = new MpaRating();
        rating.setId(ratingId);

        Integer filmId = resultSet.getInt("film_id");

        Film film = Film.builder()
                .id(filmId)
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("releasedate").toLocalDate())
                .duration(resultSet.getInt("duration"))
                .mpa(rating)
                .build();
        return film;
    }

    private void updateGenres(Film film) {
        final List<Integer> newGenresId = new ArrayList<>();
        if (film.getGenres() != null) {
            film.getGenres().stream().map(Genre::getId).forEach(newGenresId::add);
        }

        String sqlQueryGetGenresId = "select genre_id from film_genres where film_id = ?";
        List<Integer> oldGenresId = jdbcTemplate.queryForList(sqlQueryGetGenresId, Integer.class, film.getId());

        List<Integer> deleteGenres = oldGenresId.stream()
                .filter(genreId -> !newGenresId.contains(genreId))
                .collect(Collectors.toList());

        List<Integer> addGenres = newGenresId.stream()
                .filter(genreId -> !oldGenresId.contains(genreId))
                .collect(Collectors.toList());

        String sqlQueryRemoveGenres = "delete from film_genres where genre_id = ? and film_id = ?";
        if (deleteGenres.size() > 0) {
            deleteGenres.forEach((genreId) -> jdbcTemplate.update(sqlQueryRemoveGenres, genreId, film.getId()));
        }

        String sqlQueryInsertGenres = "insert into film_genres(film_id, genre_id) values(?, ?)";
        if (addGenres.size() > 0) {
            addGenres.forEach((genreId) -> jdbcTemplate.update(sqlQueryInsertGenres, film.getId(), genreId));
        }
    }

    private void updateUserLikes(Film film) {
        Set<Integer> newUserLikes = new HashSet<>();
        if (film.getUsersLikes() != null) {
            newUserLikes.addAll(film.getUsersLikes());
        }

        Set<Integer> oldUserLikes = getFilmById(film.getId()).getUsersLikes();

        List<Integer> deleteLikes = oldUserLikes.stream()
                .filter(userId -> !newUserLikes.contains(userId))
                .collect(Collectors.toList());

        List<Integer> addLikes = newUserLikes.stream()
                .filter(userId -> !oldUserLikes.contains(userId))
                .collect(Collectors.toList());

        String sqlQueryRemoveLike = "delete from user_likes where film_id = ? and user_id = ?";
        if (deleteLikes.size() > 0) {
            deleteLikes.forEach((userId) -> jdbcTemplate.update(sqlQueryRemoveLike, film.getId(), userId));
        }

        String sqlQueryInsertLikes = "insert into user_likes(film_id, user_id) values(?, ?)";
        if (addLikes.size() > 0) {
            addLikes.forEach((userId) -> jdbcTemplate.update(sqlQueryInsertLikes, film.getId(), userId));
        }
    }

    private void updateOnlyFilm(Film film) {
        String sqlQueryUpdateFilm = "update film set " +
                "name = ?, description = ?, releaseDate = ?, duration = ?, mpa_rating_id = ? where film_id = ?";
        Integer ratingId;
        if (film.getMpa() != null) {
            ratingId = film.getMpa().getId();
        } else {
            ratingId = Types.INTEGER;
        }

        jdbcTemplate.update(sqlQueryUpdateFilm,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                ratingId,
                film.getId());
    }
}