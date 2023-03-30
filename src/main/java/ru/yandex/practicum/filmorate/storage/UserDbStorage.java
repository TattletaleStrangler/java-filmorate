package ru.yandex.practicum.filmorate.storage;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Primary
@Component
@AllArgsConstructor
public class UserDbStorage implements UserStorage {
    private JdbcTemplate jdbcTemplate;

    @Override
    public User createUser(User user) {
        String sqlQueryInsertUser = "insert into users(name, login, email, birthday) values(?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQueryInsertUser, new String[]{"user_id"});
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getEmail());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());

        String sqlQueryInsertFriends = "insert into friends(from_user, to_user) values(?, ?)";
        user.getFriends()
                .stream()
                .forEach((friend_id) -> {
                    jdbcTemplate.update(sqlQueryInsertFriends, user.getId(), friend_id);
                });

        return user;
    }

    @Override
    public User getUserById(Integer id) {
        try {
            String sqlQueryUser = "select * from users where user_id = ?";
            User user = jdbcTemplate.queryForObject(sqlQueryUser, this::rowMapToUser, id);

            String sqlQueryFriends = "select friend_id from friends where user_id = ?";

            List<Integer> friendsId = jdbcTemplate.queryForList(sqlQueryFriends, Integer.class, id);
            friendsId.forEach(user::addFriend);
            return user;
        } catch (DataAccessException e) {
            return null;
        }
    }

    @Override
    public List<User> getAll() {
        String sqlQuery = "select * from users";
        List<User> users = jdbcTemplate.query(sqlQuery, this::rowMapToUser);

        String sqlQueryFriends = "select friend_id from friends where user_id = ?";
        for (User user : users) {
            List<Integer> friendsId = jdbcTemplate.queryForList(sqlQueryFriends, Integer.class, user.getId());
            friendsId.forEach(user::addFriend);
        }

        return users;
    }

    @Override
    public void deleteUserById(Integer userId) {
        String sqlQuery = "delete from users where user_id = ?";
        jdbcTemplate.update(sqlQuery, userId);
    }

    @Override
    public User updateUser(User user) {
        try {
            updateFriends(user);

            String sqlQuery = "update users set name = ?, login = ?, email = ?, birthday = ? where user_id = ?";
            jdbcTemplate.update(sqlQuery,
                    user.getName(),
                    user.getLogin(),
                    user.getEmail(),
                    user.getBirthday(),
                    user.getId());
            return user;
        } catch (DataAccessException e) {
            return null;
        }
    }

    private User rowMapToUser(ResultSet resultSet, int rowNum) throws SQLException {
        User user = User.builder()
                .id(resultSet.getInt("user_id"))
                .name(resultSet.getString("name"))
                .login(resultSet.getString("login"))
                .email(resultSet.getString("email"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .build();
        return user;
    }

    private void updateFriends(User user) {
        String sqlQueryFriends = "select friend_id from friends where user_id = ?";
        List<Integer> oldFriends = jdbcTemplate.queryForList(sqlQueryFriends, Integer.class, user.getId());

        Set<Integer> newFriends = new HashSet<>();
        if (user.getFriends() != null) {
            newFriends.addAll(user.getFriends());
        }

        List<Integer> deleteFriends = oldFriends.stream()
                .filter((friend) -> !newFriends.contains(friend))
                .collect(Collectors.toList());

        List<Integer> addFriends = newFriends.stream()
                .filter((friend) -> !oldFriends.contains(friend))
                .collect(Collectors.toList());

        String sqlQueryDeleteFriend = "delete from friends where user_id = ? and friend_id = ?";
        deleteFriends.forEach((friendId) -> jdbcTemplate.update(sqlQueryDeleteFriend, user.getId(), friendId));

        String sqlQueryAddFriend = "insert into friends(user_id, friend_id) values(?, ?)";
        addFriends.forEach((friendId) -> jdbcTemplate.update(sqlQueryAddFriend, user.getId(), friendId));
    }
}
