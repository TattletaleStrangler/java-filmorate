package ru.yandex.practicum.filmorate.storage.genre_storage;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class GenreDbStorage implements GenreStorage {
    private JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Genre mapRowToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        Genre genre = new Genre(
                resultSet.getInt("genre_id"),
                resultSet.getString("name")
        );
        return genre;
    }

    @Override
    public Genre createGenre(Genre genre) {
        String sqlQuery = "insert into genre(name) values (?)";
        jdbcTemplate.update(sqlQuery, genre.getName());
        return genre;
    }

    @Override
    public Genre getGenreById(Integer id) {
        try {
            String sqlQuery = "select * from genre where genre_id = ?";
            return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToGenre, id);
        } catch (DataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Genre> getAll() {
        String sqlQuery = "select * from genre";
        return jdbcTemplate.query(sqlQuery, this::mapRowToGenre);
    }

    @Override
    public List<Genre> getGenresByFilmId(Integer filmId) {
        try {
            String sqlQuery = "select * from genre where genre_id in (select genre_id from film_genres where film_id = ?)";
            return jdbcTemplate.query(sqlQuery, this::mapRowToGenre, filmId);
        } catch (DataAccessException e) {
            return null;
        }
    }

    @Override
    public void deleteGenreById(Integer genreId) {
        String sqlQuery = "delete from genre where genre_id = ?";
        jdbcTemplate.update(sqlQuery, genreId);
    }

    @Override
    public Genre updateGenre(Genre genre) {
        try {
            String sqlQuery = "update genre set name = ? where genre_id = ?";
            jdbcTemplate.update(sqlQuery, genre.getName(), genre.getId());
            return genre;
        } catch (DataAccessException e) {
            return null;
        }
    }
}
