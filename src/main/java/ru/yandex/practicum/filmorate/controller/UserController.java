package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserService userService;
    private Integer nextId = 1;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getUsers() {
        log.info("Получен запрос к эндпоинту: 'GET /users'");
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable(name = "id") Integer userId) {
        log.info("Получен запрос к эндпоинту: 'GET /users/{}'", userId);
        return userService.getUserById(userId);
    }

    @PostMapping
    public User saveUser(@Valid @RequestBody User user) {
        log.info("Получен запрос к эндпоинту: 'POST /users', тело запроса: {}", user);
        User savedUser = userService.createUser(user);
        log.info("Пользователь с id = " + savedUser.getId() + " сохранен.");
        return savedUser;
    }

    @PutMapping
    public User putUsers(@Valid @RequestBody User user) {
        log.info("Получен запрос к эндпоинту: 'PUT /users', тело запроса: {}", user);
        User updatedUser = userService.updateUser(user);
        log.info("Пользователь с id = " + user.getId() + " обновлен.");
        return updatedUser;
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable Integer userId) {
        log.info("Получен запрос к эндпоинту: 'DELETE /users/{}'", userId);
        userService.deleteUserById(userId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable(name = "id") Integer userId, @PathVariable(name = "friendId") Integer friendId) {
        log.info("Получен запрос к эндпоинту: 'PUT /users/{}/friends/{}'", userId, friendId);
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable(name = "id") Integer userId, @PathVariable(name = "friendId") Integer friendId) {
        log.info("Получен запрос к эндпоинту: 'DELETE /users/{}/friends/{}'", userId, friendId);
        userService.deleteFriend(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getUserFriends(@PathVariable(name = "id") Integer userId) {
        log.info("Получен запрос к эндпоинту: 'GET /users/{}/friends'", userId);
        return userService.getUserFriends(userId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable(name = "id") Integer userId
            , @PathVariable(name = "otherId") Integer otherId) {
        log.info("Получен запрос к эндпоинту: 'GET /users/{}/friends/common/{}'", userId, otherId);
        return userService.getCommonFriends(userId, otherId);
    }

    private Integer generateId() {
        return nextId++;
    }
}
