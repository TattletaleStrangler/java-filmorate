package ru.yandex.practicum.filmorate.exception;

public class NotFoundException extends RuntimeException{

    public static void throwException(String format, Integer id) {
        String message = String.format(format, id);
        throw new NotFoundException(message);
    }

    public NotFoundException(String message) {
        super(message);
    }
}
