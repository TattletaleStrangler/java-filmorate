package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validator.ValidateService;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private Map<Integer, User> users = new HashMap<>();
    private Integer nextId = 1;
    private ValidateService validateService;

    public UserController(ValidateService validateService) {
        this.validateService = validateService;
    }

    @GetMapping
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User saveUser(@Valid @RequestBody User user) {
        log.info("Получен запрос к эндпоинту: 'POST/users', тело запроса: {}", user);

        if (user.getId() != null && users.containsKey(user.getId())) {
            String message = "Пользователь уже существует.";
            log.warn(message);
            throw new ValidationException(message);
        }

        validateService.validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        final int id = generateId();
        user.setId(id);
        users.put(id, user);
        log.info("Пользователь с id = " + id + " сохранен.");
        return user;
    }

    @PutMapping
    public User putUsers(@Valid @RequestBody User user) {
        log.info("Получен запрос к эндпоинту: 'PUT/users', тело запроса: {}", user);

        if (user.getId() == null || !users.containsKey(user.getId())) {
            String message = "Пользователя не существует.";
            log.warn(message);
            throw new ValidationException(message);
        }

        validateService.validateUser(user);
        users.put(user.getId(), user);
        log.info("Пользователь с id = " + user.getId() + " обновлен.");
        return user;
    }

    private Integer generateId() {
        return nextId++;
    }
}
