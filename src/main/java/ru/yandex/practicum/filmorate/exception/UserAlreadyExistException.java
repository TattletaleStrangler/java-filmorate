package ru.yandex.practicum.filmorate.exception;

public class UserAlreadyExistException extends RuntimeException{
    public UserAlreadyExistException() {
        super("Пользователь уже существует!");
    }
}
