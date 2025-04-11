package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserSaveDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserControllerTest {
    private final ObjectMapper mapper;
    @MockBean
    private final UserService userService;
    private final MockMvc mvc;
    private UserDto userDto;

    @BeforeEach
    public void setUp() {
        userDto = new UserDto(1L, "Floyd", "wrupnk@gmail.com");
    }

    @Test
    void testAddUser() throws Exception {
        when(userService.addUser(any(UserSaveDto.class)))
                .thenReturn(userDto);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().json(mapper.writeValueAsString(userDto)));

        verify(userService, times(1)).addUser(any(UserSaveDto.class));
    }

    @Test
    void testGetAllUsers() throws Exception {
        List<UserDto> users = List.of(userDto);

        when(userService.getAllUsers())
                .thenReturn(users);

        mvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(users)));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testGetUserById() throws Exception {
        Long userId = userDto.getId();

        when(userService.getUserById(userId))
                .thenReturn(userDto);

        mvc.perform(get("/users/{userId}", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(userDto)));

        verify(userService, times(1)).getUserById(anyLong());
    }

    @Test
    void testUpdateUser() throws Exception {
        when(userService.updateUser(anyLong(), any(UserSaveDto.class)))
                .thenReturn(userDto);

        mvc.perform(patch("/users/{userId}", userDto.getId())
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(userDto)));

        verify(userService, times(1)).updateUser(anyLong(), any(UserSaveDto.class));
    }

    @Test
    void testDeleteUser() throws Exception {
        mvc.perform(delete("/users/{userId}", userDto.getId()))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(anyLong());
    }
}
