package ru.yandex.practicum.filmorate.storage.user_storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User createUser(User user);

    User getUserById(Integer id);

    List<User> getAll();

    void deleteUserById(Integer userId);

    User updateUser(User user);
}
