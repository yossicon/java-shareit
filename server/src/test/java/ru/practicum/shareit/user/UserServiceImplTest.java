package ru.practicum.shareit.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserSaveDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImplTest {
    private final EntityManager em;
    private final UserService userService;
    private final UserMapper userMapper;

    private UserSaveDto userDto1;
    private UserSaveDto userDto2;

    @BeforeEach
    public void setUp() {
        userDto1 = new UserSaveDto("Floyd", "wrupnk@gmail.com");
        userDto2 = new UserSaveDto("Alice", "ainchns@gmail.com");
    }

    @Test
    void testAddUser() {
        userService.addUser(userDto1);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", userDto1.getEmail())
                .getSingleResult();

        assertThat(user, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(userDto1.getName())),
                hasProperty("email", equalTo(userDto1.getEmail()))
        ));
    }

    @Test
    void testAddUserWithDuplicatedEmail() {
        userService.addUser(userDto1);
        userDto2.setEmail(userDto1.getEmail());

        assertThrows(DuplicatedDataException.class, () -> userService.addUser(userDto2));
    }

    @Test
    void testGetAllUsers() {
        List<UserSaveDto> sourceUsers = List.of(userDto1, userDto2);

        for (UserSaveDto userSaveDto : sourceUsers) {
            User user = userMapper.mapToUser(userSaveDto);
            em.persist(user);
        }
        em.flush();

        List<UserDto> targetUsers = userService.getAllUsers();

        assertThat(targetUsers, hasSize(sourceUsers.size()));
        for (UserSaveDto sourceUser : sourceUsers) {
            assertThat(targetUsers, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceUser.getName())),
                    hasProperty("email", equalTo(sourceUser.getEmail()))
            )));
        }
    }

    @Test
    void testGetUserById() {
        User user = userMapper.mapToUser(userDto1);
        em.persist(user);
        em.flush();

        UserDto userDto = userService.getUserById(user.getId());

        assertThat(userDto, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(userDto1.getName())),
                hasProperty("email", equalTo(userDto1.getEmail()))
        ));
    }

    @Test
    void testUpdateUser() {
        User user = userMapper.mapToUser(userDto1);
        em.persist(user);
        em.flush();

        UserSaveDto userSaveDto = userDto1;
        userSaveDto.setName("Pink");
        user.setEmail("wrufld@gmail.com");
        UserDto updatedUser = userService.updateUser(user.getId(), userSaveDto);

        assertThat(updatedUser, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(userSaveDto.getName())),
                hasProperty("email", equalTo(userSaveDto.getEmail()))
        ));
    }

    @Test
    void testUpdateUserWithDuplicatedEmail() {
        User user = userMapper.mapToUser(userDto1);
        em.persist(user);
        User otherUser = userMapper.mapToUser(userDto2);
        em.persist(otherUser);
        em.flush();

        UserSaveDto userSaveDto = userDto1;
        userSaveDto.setName("Pink");
        userSaveDto.setEmail(otherUser.getEmail());

        assertThrows(DuplicatedDataException.class, () -> userService.updateUser(user.getId(), userSaveDto));
    }

    @Test
    void testDeleteUser() {
        User user = userMapper.mapToUser(userDto1);
        em.persist(user);
        em.flush();

        userService.deleteUser(user.getId());

        assertThrows(NotFoundException.class, () -> userService.deleteUser(user.getId()));
    }
}
