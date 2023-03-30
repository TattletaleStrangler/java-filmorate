package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserDbStorageTest {
    private final UserDbStorage userStorage;
    public User user1;
    public User user2;
    public User user3;

    @BeforeEach
    public void beforeEach() {
        user1 = new User();
        user2 = new User();
        user3 = new User();

        List<User> users = List.of(user1, user2, user3);
        final int[] ordinal = new int[] {1};

        users.forEach(user -> {
            user.setName("Name" + ordinal[0]);
            user.setEmail(ordinal[0] + "user@email.ru");
            user.setLogin(ordinal[0] + "userLogin");
            user.setBirthday(LocalDate.of(1950 + ordinal[0] * 10, 1 + ordinal[0], 1 + ordinal[0] * 2));
            ordinal[0]++;
        });
    }

    @Test
    void testCreateUser() {
        userStorage.createUser(user1);

        Optional<User> userOptional = Optional.ofNullable(userStorage.getUserById(1));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1)
                );
    }

    @Test
    void testGetUserById() {
        userStorage.createUser(user1);

        Optional<User> userOptional = Optional.ofNullable(userStorage.getUserById(1));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", 1);
                    assertThat(user).hasFieldOrPropertyWithValue("email", user1.getEmail());
                    assertThat(user).hasFieldOrPropertyWithValue("name", user1.getName());
                    assertThat(user).hasFieldOrPropertyWithValue("login", user1.getLogin());
                    assertThat(user).hasFieldOrPropertyWithValue("birthday", user1.getBirthday());
                });
    }

    @Test
    void testGetAll() {
        userStorage.createUser(user1);
        userStorage.createUser(user2);
        userStorage.createUser(user3);

        user1.setId(1);
        user2.setId(2);
        user3.setId(3);

        List<User> expectedUsers = List.of(user1, user2, user3);

        assertIterableEquals(expectedUsers, userStorage.getAll(), "списки пользователей не совпадают");
    }

    @Test
    void testDeleteUserById() {
        User createdUser = userStorage.createUser(user1);

        userStorage.deleteUserById(createdUser.getId());

        Optional<User> userOptional = Optional.ofNullable(userStorage.getUserById(createdUser.getId()));

        assertFalse(userOptional.isPresent());
    }

    @Test
    void testUpdateUser() {
        userStorage.createUser(user1);

        String updatedName = "Updated Name";
        String updatedLogin = "UpdatedLogin";
        String updatedEmail = "updated@email";
        LocalDate updatedBirthday = LocalDate.of(1999, 10, 10);

        user1.setName(updatedName);
        user1.setLogin(updatedLogin);
        user1.setEmail(updatedEmail);
        user1.setBirthday(updatedBirthday);

        userStorage.updateUser(user1);

        Optional<User> userOptional = Optional.ofNullable(userStorage.getUserById(1));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", 1);
                    assertThat(user).hasFieldOrPropertyWithValue("name", updatedName);
                    assertThat(user).hasFieldOrPropertyWithValue("login", updatedLogin);
                    assertThat(user).hasFieldOrPropertyWithValue("email", updatedEmail);
                    assertThat(user).hasFieldOrPropertyWithValue("birthday", updatedBirthday);
                });
    }
}