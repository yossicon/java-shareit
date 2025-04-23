package ru.practicum.shareit.request;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserSaveDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceImplTest {
    private static final Long NONEXISTENT_ID = 100L;

    private final EntityManager em;
    private final ItemRequestService requestService;
    private final UserService userService;

    private ItemRequestSaveDto requestDto1;
    private ItemRequestSaveDto requestDto2;
    private UserSaveDto userDto1;
    private UserSaveDto userDto2;

    @BeforeEach
    public void setUp() {
        requestDto1 = new ItemRequestSaveDto("I want to book a bicycle");
        requestDto2 = new ItemRequestSaveDto("Need a hammer");

        userDto1 = new UserSaveDto("Floyd", "wrupnk@gmail.com");
        userDto2 = new UserSaveDto("Alice", "ainchns@gmail.com");
    }

    @Test
    void testAddRequest() {
        UserDto user = userService.addUser(userDto1);

        requestService.addRequest(user.getId(), requestDto1);

        TypedQuery<ItemRequest> query =
                em.createQuery("Select ir from ItemRequest ir where ir.requester.id = :requesterId", ItemRequest.class);
        ItemRequest itemRequest = query.setParameter("requesterId", user.getId())
                .getSingleResult();

        assertThat(itemRequest, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("description", equalTo(requestDto1.getDescription()))
        ));
        assertThat(itemRequest.getRequester(), allOf(
                hasProperty("name", equalTo(user.getName())),
                hasProperty("email", equalTo(user.getEmail()))
        ));
    }

    @Test
    void testAddRequestNonexistentUser() {
        assertThrows(NotFoundException.class, () ->
                requestService.addRequest(NONEXISTENT_ID, requestDto1));
    }

    @Test
    void testGetAllUserRequests() {
        UserDto user = userService.addUser(userDto1);
        List<ItemRequestSaveDto> sourceRequests = List.of(requestDto1, requestDto2);
        requestService.addRequest(user.getId(), requestDto1);
        requestService.addRequest(user.getId(), requestDto2);

        List<ItemRequestDto> targetRequests = requestService.getAllUserRequests(user.getId());

        assertThat(targetRequests, hasSize(sourceRequests.size()));
        for (ItemRequestSaveDto sourceRequest : sourceRequests) {
            assertThat(targetRequests, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(sourceRequest.getDescription()))
            )));
        }
    }

    @Test
    void testGetAllRequests() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);

        requestService.addRequest(user1.getId(), requestDto1);
        requestService.addRequest(user2.getId(), requestDto2);

        List<ItemRequestDto> requests = requestService.getAllRequests(user2.getId());

        assertThat(requests, hasSize(1));
        assertThat(requests.getFirst(), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("description", equalTo(requestDto1.getDescription()))
        ));
    }

    @Test
    void testGetRequestById() {
        UserDto user = userService.addUser(userDto1);
        ItemRequestDto addedRequest = requestService.addRequest(user.getId(), requestDto1);

        ItemRequestDto requestDto = requestService.getRequestById(addedRequest.getId());

        assertThat(requestDto, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("description", equalTo(addedRequest.getDescription())),
                hasProperty("requester", equalTo(addedRequest.getRequester()))
        ));
    }
}
