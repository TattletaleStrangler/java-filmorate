package ru.yandex.practicum.filmorate.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
@Builder
public class Genre {
    private Integer id;

    @EqualsAndHashCode.Exclude
    private String name;
}