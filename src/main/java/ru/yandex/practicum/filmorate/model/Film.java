package ru.yandex.practicum.filmorate.model;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Film {
    private Integer id;
    @NotBlank
    private String name;
    @Length(min = 1, max = 200, message = "Описание фильма не должно превышать 200 символов.")
    private String description;
    private LocalDate releaseDate;
    @Min(value = 0, message = "Продолжительность фильма не может быть отрицательной.")
    private Integer duration;
}
