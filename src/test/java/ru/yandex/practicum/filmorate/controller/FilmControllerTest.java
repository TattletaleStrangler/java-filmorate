package ru.yandex.practicum.filmorate.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.utils.LocalDateAdapter;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmControllerTest {

    public final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();
    public Film film1;
    public Film film2;
    public Film film3;

    @Autowired
    private TestRestTemplate restTemplate;

    private final String URL = "/films";

    @BeforeEach
    public void beforeEach() {
        film1 = new Film();
        film2 = new Film();
        film3 = new Film();

        List<Film> films = List.of(film1, film2, film3);
        int i = 1;

        for (Film film : films) {
            film.setName("Name" + i);
            film.setDescription("Description" + i);
            film.setDuration(100 + i * 5);
            film.setReleaseDate(LocalDate.of(1900 + i * 10, 1 + i, 1 + i * 2));
            i++;
        }
    }

    @Test
    void getFilms() {
        ResponseEntity<Film> response1 = restTemplate.postForEntity(URL, film1, Film.class);
        ResponseEntity<Film> response2 = restTemplate.postForEntity(URL, film2, Film.class);
        ResponseEntity<Film> response3 = restTemplate.postForEntity(URL, film3, Film.class);

        film1.setId(1);
        film2.setId(2);
        film3.setId(3);
        List<Film> films = List.of(film1, film2, film3);
        String expected = gson.toJson(films);

        String response = this.restTemplate.getForObject(URL, String.class);

        assertEquals(expected, response, "Списки фильмов не совпадают.");
    }


    @Test
    void saveFilm() {
        final ResponseEntity<Film> response = restTemplate.postForEntity(URL, film1, Film.class);

        film1.setId(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(film1, response.getBody());

        List<Film> films = List.of(film1);
        String expected = gson.toJson(films);

        final ResponseEntity<String> receivedFilm = this.restTemplate.getForEntity(URL, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, receivedFilm.getBody(), "Списки фильмов не совпадают.");
    }

    @Test
    void putFilm() {
        final ResponseEntity<Film> response = restTemplate.postForEntity(URL, film1, Film.class);

        film1 = response.getBody();
        film1.setName("newName");
        film1.setDescription("newDescription");

        HttpEntity<Film> entity = new HttpEntity<>(film1);
        ResponseEntity<Film> responsePut = restTemplate.exchange(URL, HttpMethod.PUT, entity, Film.class);

        assertEquals(HttpStatus.OK, responsePut.getStatusCode());
        assertEquals(film1, responsePut.getBody());

        List<Film> films = List.of(film1);
        String expected = gson.toJson(films);

        final ResponseEntity<String> receivedFilm = this.restTemplate.getForEntity(URL, String.class);
        assertEquals(HttpStatus.OK, receivedFilm.getStatusCode());
        assertEquals(expected, receivedFilm.getBody(), "Списки фильмов не совпадают.");
    }

    @Test
    void shouldReturnStatus500WhenEmptyName() {
        film1.setName("");
        final ResponseEntity<Film> response = restTemplate.postForEntity(URL, film1, Film.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(new Film(), response.getBody());
    }

    @Test
    void shouldReturnStatus500WhenDescriptionMoreThen200symbols() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 1; i <= 201; i++) {
            stringBuilder.append(i);
        }

        String description = stringBuilder.toString();
        film1.setDescription(description);

        final ResponseEntity<Film> response = restTemplate.postForEntity(URL, film1, Film.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(new Film(), response.getBody());
    }

    @Test
    void shouldReturnStatus500WhenReleaseDateIsEarlierThanCinemaBirthday() {
        film1.setReleaseDate(LocalDate.of(1895, 12, 27));

        final ResponseEntity<Film> response = restTemplate.postForEntity(URL, film1, Film.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(new Film(), response.getBody());
    }

    @Test
    void shouldReturnStatus500WhenDurationIsNegative() {
        film1.setDuration(-1);

        final ResponseEntity<Film> response = restTemplate.postForEntity(URL, film1, Film.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(new Film(), response.getBody());
    }
}