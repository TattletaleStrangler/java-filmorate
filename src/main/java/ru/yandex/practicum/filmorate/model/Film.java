package ru.yandex.practicum.filmorate.model;

import lombok.*;
import org.hibernate.validator.constraints.Length;
import ru.yandex.practicum.filmorate.validator.LateDate;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
@Data
@Builder
public class Film {
    private Integer id;

    @NotBlank
    private String name;

    @Length(min = 1, max = 200, message = "Описание фильма не должно превышать 200 символов.")
    private String description;

    @LateDate(day = "1895-12-28", formatter = "yyyy-MM-dd", message = "Дата выхода фильма не может быть раньше дня рождения кино")
    private LocalDate releaseDate;

    @Min(value = 0, message = "Продолжительность фильма не может быть отрицательной.")
    private Integer duration;

    private List<Genre> genres;

    private MpaRating mpa;

    private final Set<Integer> usersLikes = new HashSet<>();

    public void addLike(Integer userId) {
        usersLikes.add(userId);
    }

    public void removeLike(Integer userId) {
        usersLikes.remove(userId);
    }
}
