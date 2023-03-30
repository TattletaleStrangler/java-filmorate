package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user_storage.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final String notFoundUserFormat = "Пользователь с id = %d не найден.";

    public User createUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        checkUserFriends(user);
        return userStorage.createUser(user);
    }

    public User getUserById(Integer userId) {
        User user = userStorage.getUserById(userId);
        if (user == null) {
            log.warn(String.format(notFoundUserFormat, userId));
            NotFoundException.throwException(notFoundUserFormat, userId);
        }
        return user;
    }

    public List<User> getUsers() {
        return userStorage.getAll();
    }

    public void deleteUserById(Integer userId) {
        User user = userStorage.getUserById(userId);
        if (user == null) {
            NotFoundException.throwException(notFoundUserFormat, userId);
        }
        userStorage.deleteUserById(userId);
    }

    public User updateUser(User user) {
        Integer userId = user.getId();
        if (userId == null || userStorage.getUserById(userId) == null) {
            log.warn(String.format(notFoundUserFormat, userId));
            NotFoundException.throwException(notFoundUserFormat, userId);
        }
        checkUserFriends(user);
        return userStorage.updateUser(user);
    }

    public void addFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        if (user == null) {
            log.warn(String.format(notFoundUserFormat, userId));
            NotFoundException.throwException(notFoundUserFormat, userId);
        }
        if (friend == null) {
            log.warn(String.format(notFoundUserFormat, friendId));
            NotFoundException.throwException(notFoundUserFormat, friendId);
        }

        user.addFriend(friendId);

        userStorage.updateUser(user);
    }

    public void deleteFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        if (user == null) {
            log.warn(String.format(notFoundUserFormat, userId));
            NotFoundException.throwException(notFoundUserFormat, userId);
        }
        if (friend == null) {
            log.warn(String.format(notFoundUserFormat, friendId));
            NotFoundException.throwException(notFoundUserFormat, friendId);
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
            NotFoundException.throwException(notFoundUserFormat, userId);
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
            NotFoundException.throwException(notFoundUserFormat, userId);
        }
        if (other == null) {
            log.warn(String.format(notFoundUserFormat, otherId));
            NotFoundException.throwException(notFoundUserFormat, otherId);
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

    private void checkUserFriends(User user) {
        Set<Integer> friends = user.getFriends();
        if (friends != null) {
            friends.forEach((friend) -> {
                User friendFromDb = userStorage.getUserById(friend);
                if (friendFromDb == null) {
                    log.warn(String.format(notFoundUserFormat, friend));
                    NotFoundException.throwException(notFoundUserFormat, friend);
                }
            });
        }
    }
}
