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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.util.LocalDateAdapter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

    public final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();

    public User user1;
    public User user2;
    public User user3;

    @Autowired
    private TestRestTemplate restTemplate;

    private final String url = "/users";

    @BeforeEach
    public void beforeEach() {
        user1 = new User();
        user2 = new User();
        user3 = new User();

        List<User> users = List.of(user1, user2, user3);
        final int[] ordinal = new int[]{1};

        users.forEach(user -> {
            user.setName("Name" + ordinal[0]);
            user.setEmail(ordinal[0] + "user@email.ru");
            user.setLogin(ordinal[0] + "userLogin");
            user.setFriends(new HashSet<>());
            user.setBirthday(LocalDate.of(1950 + ordinal[0] * 10, 1 + ordinal[0], 1 + ordinal[0] * 2));
            ordinal[0]++;
        });
    }

    @Test
    void getUsers() {
        restTemplate.postForEntity(url, user1, User.class);
        restTemplate.postForEntity(url, user2, User.class);
        restTemplate.postForEntity(url, user3, User.class);

        user1.setId(1);
        user2.setId(2);
        user3.setId(3);
        List<User> users = List.of(user1, user2, user3);
        String expected = gson.toJson(users);

        String response = this.restTemplate.getForObject(url, String.class);

        assertEquals(expected, response, "Списки пользователей не совпадают.");
    }

    @Test
    void saveUser() {
        final ResponseEntity<User> response = restTemplate.postForEntity(url, user1, User.class);

        user1.setId(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user1, response.getBody());

        List<User> users = List.of(user1);
        String expected = gson.toJson(users);

        final ResponseEntity<String> receivedUser = this.restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, receivedUser.getBody(), "Списки фильмов не совпадают.");

    }

    @Test
    void putUsers() {
        final ResponseEntity<User> response = restTemplate.postForEntity(url, user1, User.class);

        user1 = response.getBody();
        user1.setName("newName");
        user1.setEmail("new@email.ru");

        HttpEntity<User> entity = new HttpEntity<>(user1);
        ResponseEntity<User> responsePut = restTemplate.exchange(url, HttpMethod.PUT, entity, User.class);

        assertEquals(HttpStatus.OK, responsePut.getStatusCode());
        assertEquals(user1, responsePut.getBody());

        List<User> users = List.of(user1);
        String expected = gson.toJson(users);

        final ResponseEntity<String> receivedUser = this.restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.OK, receivedUser.getStatusCode());
        assertEquals(expected, receivedUser.getBody(), "Списки пользователей не совпадают.");

    }

    @Test
    void shouldReturnStatusOKWhenEmptyName() {
        user1.setName("");
        final ResponseEntity<User> response = restTemplate.postForEntity(url, user1, User.class);
        user1.setId(1);
        user1.setName(user1.getLogin());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user1, response.getBody());
    }

    @Test
    void shouldReturnStatus500WhenEmailIsInvalid() {
        user1.setEmail("email");
        final ResponseEntity<User> response = restTemplate.postForEntity(url, user1, User.class);

        List<HttpStatus> expected = List.of(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.BAD_REQUEST);
        assertTrue(expected.contains(response.getStatusCode()), "Коды ответа не совпадают.");
        assertEquals(new User(), response.getBody(), "Тело ответа не соответствует ожидаемому.");
    }

    @Test
    void shouldReturnStatus500WhenLoginContainsSpace() {
        user1.setLogin("Log in");
        final ResponseEntity<User> response = restTemplate.postForEntity(url, user1, User.class);

        List<HttpStatus> expected = List.of(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.BAD_REQUEST);
        assertTrue(expected.contains(response.getStatusCode()), "Коды ответа не совпадают.");
        assertEquals(new User(), response.getBody(), "Тело ответа не соответствует ожидаемому.");
    }

    @Test
    void shouldReturnStatus500WhenEmptyLogin() {
        user1.setLogin("");
        final ResponseEntity<User> response = restTemplate.postForEntity(url, user1, User.class);

        List<HttpStatus> expected = List.of(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.BAD_REQUEST);
        assertTrue(expected.contains(response.getStatusCode()), "Коды ответа не совпадают.");
        assertEquals(new User(), response.getBody(), "Тело ответа не соответствует ожидаемому.");
    }

    @Test
    void shouldReturnStatus500WhenBirthdayInFuture() {
        user1.setBirthday(LocalDate.now().plusDays(1));
        final ResponseEntity<User> response = restTemplate.postForEntity(url, user1, User.class);

        List<HttpStatus> expected = List.of(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.BAD_REQUEST);
        assertTrue(expected.contains(response.getStatusCode()), "Коды ответа не совпадают.");
        assertEquals(new User(), response.getBody(), "Тело ответа не соответствует ожидаемому.");
    }

}