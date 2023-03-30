package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Data
@Builder
public class User {
    private Integer id;

    @Email (message = "Введенное значение не является адресом электронной почты.")
    private String email;

    @NotBlank
    @Pattern(regexp = "\\S*", message = "Логин не может содержать пробелы.")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем. ")
    private LocalDate birthday;

    private final Set<Integer> friends = new HashSet<>();

    public void addFriend(Integer userId) {
        friends.add(userId);
    }

    public void removeFriend(Integer userId) {
        friends.remove(userId);
    }
}
