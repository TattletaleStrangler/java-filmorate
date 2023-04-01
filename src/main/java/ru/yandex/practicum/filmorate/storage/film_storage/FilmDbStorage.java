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
import ru.yandex.practicum.filmorate.util.GenresExtractor;
import ru.yandex.practicum.filmorate.util.LikesExtractor;

import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Primary
@Component
@AllArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final LikesExtractor likesExtractor;
    private final GenresExtractor genresExtractor;

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

        if (film.getGenres() != null) {
            List<Integer> genresId = film.getGenres().stream().map(Genre::getId).collect(Collectors.toList());
            insertFilmGenres(film.getId(), genresId);
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

        addGenresToFilms(films);
        addLikesToFilms(films);

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

    private void insertFilmGenres(Integer filmId, List<Integer> genresId) {
        if (genresId != null && genresId.size() > 0) {
            String sqlInsertGenres = "insert into film_genres(film_id, genre_id) values";
            StringBuilder sqlQuery = new StringBuilder(sqlInsertGenres);
            genresId.forEach(genreId -> sqlQuery.append(String.format("(%d, %d),", filmId, genreId)));
            sqlQuery.deleteCharAt(sqlQuery.length() - 1);

            jdbcTemplate.update(sqlQuery.toString());
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
        film.setUsersLikes(new HashSet<>(userLikes));

        return film;
    }

    private void addGenresToFilms(List<Film> films) {
        if (films.size() == 0) {
            return;
        }

        String sqlGenres = "select film_id, genre_id from film_genres where film_id IN (";
        StringBuilder sqlQueryGenres = new StringBuilder(sqlGenres);
        for (Film film : films) {
            sqlQueryGenres.append(film.getId()).append(",");
        }
        sqlQueryGenres.deleteCharAt(sqlQueryGenres.length() - 1);
        sqlQueryGenres.append(")");

        Map<Integer, Set<Integer>> filmsIdAndTheirGenresId = jdbcTemplate.query(sqlQueryGenres.toString(), genresExtractor);

        for (Film film : films) {
            if (filmsIdAndTheirGenresId.containsKey(film.getId())) {
                List<Genre> genresWithoutName = filmsIdAndTheirGenresId.get(film.getId())
                        .stream()
                        .map((genreId) -> {
                            Genre genre = new Genre();
                            genre.setId(genreId);
                            return genre;
                        })
                        .collect(Collectors.toList());
                film.setGenres(genresWithoutName);
            } else {
                film.setGenres(new ArrayList<>());
            }
        }
    }

    private void addLikesToFilms(List<Film> films) {
        if (films.size() == 0) {
            return;
        }

        String sqlLikes = "select user_id, film_id from user_likes where film_id IN (";
        StringBuilder sqlQueryLikes = new StringBuilder(sqlLikes);
        for (Film film : films) {
            sqlQueryLikes.append(film.getId()).append(",");
        }
        sqlQueryLikes.deleteCharAt(sqlQueryLikes.length() - 1);
        sqlQueryLikes.append(")");

        Map<Integer, Set<Integer>> filmsIdAndTheirLikesId = jdbcTemplate.query(sqlQueryLikes.toString(), likesExtractor);

        for (Film film : films) {
            if (filmsIdAndTheirLikesId.containsKey(film.getId())) {
                film.setUsersLikes(filmsIdAndTheirLikesId.get(film.getId()));
            } else {
                film.setUsersLikes(new HashSet<>());
            }
        }
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
        final Set<Integer> newGenresId = new HashSet<>();
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

        deleteFilmGenres(film.getId(), deleteGenres);
        insertFilmGenres(film.getId(), addGenres);
    }

    private void deleteFilmGenres(Integer filmId, List<Integer> genresId) {
        if (genresId != null && genresId.size() > 0) {
            String sqlDeleteGenres = "delete from film_genres where film_id = ? and genre_id IN (";
            StringBuilder sqlQuery = new StringBuilder(sqlDeleteGenres);
            genresId.forEach(genreId -> sqlQuery.append(String.format("%d,", genreId)));
            sqlQuery.deleteCharAt(sqlQuery.length() - 1);
            sqlQuery.append(")");

            jdbcTemplate.update(sqlQuery.toString(), filmId);
        }
    }

    private void updateUserLikes(Film film) {
        Set<Integer> newUserLikes = new HashSet<>();
        if (film.getUsersLikes() != null) {
            newUserLikes.addAll(film.getUsersLikes());
        }

        Set<Integer> oldUserLikes = findFilmById(film.getId()).getUsersLikes();

        List<Integer> deleteLikes = oldUserLikes.stream()
                .filter(userId -> !newUserLikes.contains(userId))
                .collect(Collectors.toList());

        List<Integer> addLikes = newUserLikes.stream()
                .filter(userId -> !oldUserLikes.contains(userId))
                .collect(Collectors.toList());

        deleteLikes(film.getId(), deleteLikes);
        insertLikes(film.getId(), addLikes);
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

    private void deleteLikes(Integer filmId, List<Integer> userLikes) {
        if (userLikes != null && userLikes.size() > 0) {
            String sqlDeleteLikes = "delete from user_likes where film_id = ? and user_id IN (";
            StringBuilder sqlQuery = new StringBuilder(sqlDeleteLikes);
            userLikes.forEach(userId -> sqlQuery.append(String.format("%d,", userId)));
            sqlQuery.deleteCharAt(sqlQuery.length() - 1);
            sqlQuery.append(")");

            jdbcTemplate.update(sqlQuery.toString(), filmId);
        }
    }

    private void insertLikes(Integer filmId, List<Integer> userLikes) {
        if (userLikes != null && userLikes.size() > 0) {
            String sqlInsertLikes = "insert into user_likes(film_id, user_id) values";
            StringBuilder sqlQuery = new StringBuilder(sqlInsertLikes);
            userLikes.forEach(userId -> sqlQuery.append(String.format("(%d, %d),", filmId, userId)));
            sqlQuery.deleteCharAt(sqlQuery.length() - 1);

            jdbcTemplate.update(sqlQuery.toString());
        }
    }
}
