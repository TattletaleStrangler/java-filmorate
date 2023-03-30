package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class MpaRatingDbStorage implements MpaRatingStorage {
    private JdbcTemplate jdbcTemplate;

    public MpaRatingDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private MpaRating mapRowToMpaRating(ResultSet resultSet, int rowNum) throws SQLException {
        return new MpaRating(resultSet.getInt("rating_id"),
                resultSet.getString("name"));
    }

    @Override
    public MpaRating createMpaRating(MpaRating mpaRating) {
        String sqlQuery = "insert into mpa_rating(name) values(?)";
        jdbcTemplate.update(sqlQuery, mpaRating.getName());
        return mpaRating;
    }

    @Override
    public MpaRating getMpaRatingById(Integer id) {
        try {
            String sqlQuery = "select * from mpa_rating where rating_id = ?";
            return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToMpaRating, id);
        } catch (DataAccessException e) {
            return null;
        }
    }

    @Override
    public List<MpaRating> getAll() {
        String sqlQuery = "select * from mpa_rating";
        return jdbcTemplate.query(sqlQuery, this::mapRowToMpaRating);
    }

    @Override
    public void deleteMpaRatingById(Integer mpaRatingId) {
        try {
            String sqlQuery = "delete from mpa_rating where rating_id = ?";
            jdbcTemplate.update(sqlQuery, mpaRatingId);
        } catch (DataAccessException ignored) {
        }
    }

    @Override
    public MpaRating updateMpaRating(MpaRating mpaRating) {
        try {
            String sqlQuery = "update mpa_rating set name = ? where rating_id = ?";
            jdbcTemplate.update(sqlQuery, mpaRating.getName(), mpaRating.getId());
            return mpaRating;
        } catch (DataAccessException e) {
            return null;
        }
    }
}
