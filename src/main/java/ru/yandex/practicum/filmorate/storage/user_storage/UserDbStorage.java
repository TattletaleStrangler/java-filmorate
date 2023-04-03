package ru.yandex.practicum.filmorate.storage.user_storage;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.util.FriendsExtractor;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Primary
@Component
@AllArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FriendsExtractor friendsExtractor;

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

        if (user.getFriends() != null && user.getFriends().size() > 0) {
            insertFriends(user.getId(), user.getFriends());
        }

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
        addFriendsToUser(users);
        return users;
    }

    private void addFriendsToUser(List<User> users) {
        if (users.size() == 0) {
            return;
        }

        String sqlFriends = "select user_id, friend_id from friends where user_id IN (";
        StringBuilder sqlQueryFriends = new StringBuilder(sqlFriends);
        for (User user : users) {
            sqlQueryFriends.append(user.getId()).append(",");
        }
        sqlQueryFriends.deleteCharAt(sqlQueryFriends.length() - 1);
        sqlQueryFriends.append(")");

        Map<Integer, Set<Integer>> usersIdAndTheirFriendsId = jdbcTemplate.query(sqlQueryFriends.toString(),
                friendsExtractor);

        for (User user : users) {
            if (usersIdAndTheirFriendsId.containsKey(user.getId())) {
                user.setFriends(usersIdAndTheirFriendsId.get(user.getId()));
            } else {
                user.setFriends(new HashSet<>());
            }
        }
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

    private void insertFriends(Integer userId, Set<Integer> friendsId) {
        String sqlInsertFriends = "insert into friends(user_id, friend_id) values";
        StringBuilder sqlQuery = new StringBuilder(sqlInsertFriends);
        friendsId.forEach(friendId -> sqlQuery.append(String.format("(%d, %d),"), userId, friendId));
        sqlQuery.deleteCharAt(sqlQuery.length() - 1);

        jdbcTemplate.update(sqlQuery.toString());
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

        deleteFriends(user.getId(), deleteFriends);
        insertFriends(user.getId(), addFriends);
    }

    private void deleteFriends(Integer userId, List<Integer> friendsId) {
        if (friendsId != null && friendsId.size() > 0) {
            String sqlDeleteFriends = "delete from friends where user_id = ? and friend_id IN (";
            StringBuilder sqlQuery = new StringBuilder(sqlDeleteFriends);
            friendsId.forEach(friendId -> sqlQuery.append(String.format("%d,", friendId)));
            sqlQuery.deleteCharAt(sqlQuery.length() - 1);
            sqlQuery.append(")");

            jdbcTemplate.update(sqlQuery.toString(), userId);
        }
    }

    private void insertFriends(Integer userId, List<Integer> friendsId) {
        if (friendsId != null && friendsId.size() > 0) {
            String sqlInsertFriends = "insert into friends(user_id, friend_id) values";
            StringBuilder sqlQuery = new StringBuilder(sqlInsertFriends);
            friendsId.forEach(friendId -> sqlQuery.append(String.format("(%d, %d),", userId, friendId)));
            sqlQuery.deleteCharAt(sqlQuery.length() - 1);

            jdbcTemplate.update(sqlQuery.toString());
        }
    }
}
