package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final String notFoundUserFormat = "Пользователь с id = %d не найден.";

    private void throwNotFoundException(String format, Integer id) {
        String message = String.format(format, id);
        log.warn(message);
        throw new NotFoundException(message);
    }

    public User createUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.createUser(user);
    }

    public User getUserById(Integer userId) {
        User user = userStorage.getUserById(userId);
        if (user == null) {
            log.warn(String.format(notFoundUserFormat, userId));
            throwNotFoundException(notFoundUserFormat, userId);
        }
        return user;
    }

    public List<User> getUsers() {
        return userStorage.getAll();
    }

    public void deleteUserById(Integer filmId) {
        userStorage.deleteUserById(filmId);
    }

    public User updateUser(User user) {
        Integer userId = user.getId();
        if (userId == null || userStorage.getUserById(userId) == null) {
            log.warn(String.format(notFoundUserFormat, userId));
            throwNotFoundException(notFoundUserFormat, userId);
        }
        return userStorage.updateUser(user);
    }

    public void addFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        if (user == null) {
            log.warn(String.format(notFoundUserFormat, userId));
            throwNotFoundException(notFoundUserFormat, userId);
        }
        if (friend == null) {
            log.warn(String.format(notFoundUserFormat, friendId));
            throwNotFoundException(notFoundUserFormat, friendId);
        }

        user.addFriend(friendId);
        friend.addFriend(userId);

        userStorage.updateUser(user);
        userStorage.updateUser(friend);
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        if (user == null) {
            log.warn(String.format(notFoundUserFormat, userId));
            throwNotFoundException(notFoundUserFormat, userId);
        }
        if (friend == null) {
            log.warn(String.format(notFoundUserFormat, friendId));
            throwNotFoundException(notFoundUserFormat, friendId);
        }

        user.removeFriend(friendId);
        friend.removeFriend(userId);

        userStorage.updateUser(user);
        userStorage.updateUser(friend);
    }

    public List<User> getUserFriends(Integer userId) {
        User user = userStorage.getUserById(userId);

        if (user == null) {
            log.warn(String.format(notFoundUserFormat, userId));
            throwNotFoundException(notFoundUserFormat, userId);
        }

        final List<User> userFriends = new ArrayList<>();

        if (user.getFriends() == null) {
            return userFriends;
        }

        user.getFriends().stream()
                .sorted()
                .map(userStorage::getUserById)
                .forEach(userFriends::add);
        return userFriends;
    }

    public List<User> getCommonFriends(Integer userId, Integer otherId) {
        User user = userStorage.getUserById(userId);
        User other = userStorage.getUserById(otherId);

        if (user == null) {
            log.warn(String.format(notFoundUserFormat, userId));
            throwNotFoundException(notFoundUserFormat, userId);
        }
        if (other == null) {
            log.warn(String.format(notFoundUserFormat, otherId));
            throwNotFoundException(notFoundUserFormat, otherId);
        }

        Set<Integer> userFriends = user.getFriends();
        Set<Integer> otherFriends = other.getFriends();

        final List<User> commonFriends = new ArrayList<>();
        if (userFriends == null || otherFriends == null) {
            return commonFriends;
        }

        userFriends.stream()
                .filter(otherFriends::contains)
                .sorted()
                .map(userStorage::getUserById)
                .forEach(commonFriends::add);
        return commonFriends;
    }
}
