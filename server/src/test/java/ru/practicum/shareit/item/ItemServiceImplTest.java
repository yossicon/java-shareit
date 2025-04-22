package ru.practicum.shareit.item;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingSaveDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BookingUnavailableException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserSaveDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImplTest {
    private static final Long NONEXISTENT_ID = 100L;

    private final EntityManager em;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingService bookingService;
    private final ItemRequestService requestService;

    private UserSaveDto userDto1;
    private UserSaveDto userDto2;
    private ItemSaveDto itemDto1;
    private ItemSaveDto itemDto2;
    private BookingSaveDto bookingDto1;
    private BookingSaveDto bookingDto2;
    private CommentSaveDto commentDto;
    private ItemRequestSaveDto requestDto;

    @BeforeEach
    public void setUp() {
        userDto1 = new UserSaveDto("Floyd", "wrupnk@gmail.com");
        userDto2 = new UserSaveDto("Alice", "ainchns@gmail.com");

        itemDto1 = new ItemSaveDto("Harp", "Fine harp", true, null);
        itemDto2 = new ItemSaveDto("Suit", "Fine suit", true, null);

        bookingDto1 = new BookingSaveDto();
        bookingDto1.setStart(LocalDateTime.of(2025, 2, 5, 15, 0));
        bookingDto1.setEnd(LocalDateTime.of(2025, 3, 25, 15, 0));

        bookingDto2 = new BookingSaveDto();
        bookingDto2.setStart(LocalDateTime.of(2025, 5, 5, 15, 0));
        bookingDto2.setEnd(LocalDateTime.of(2025, 5, 25, 15, 0));

        commentDto = new CommentSaveDto("It's comment");

        requestDto = new ItemRequestSaveDto("I want to book a bicycle");
    }

    @Test
    void testAddItem() {
        UserDto user = userService.addUser(userDto1);

        itemService.addItem(user.getId(), itemDto1);

        TypedQuery<Item> query = em.createQuery("Select it from Item it where it.owner.id = :ownerId", Item.class);
        Item item = query.setParameter("ownerId", user.getId())
                .getSingleResult();

        assertThat(item, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(itemDto1.getName())),
                hasProperty("description", equalTo(itemDto1.getDescription())),
                hasProperty("available", equalTo(itemDto1.getAvailable()))
        ));
    }

    @Test
    void testAddItemByRequest() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);

        ItemRequestDto itemRequestDto = requestService.addRequest(user2.getId(), requestDto);

        itemDto1.setRequestId(itemRequestDto.getId());
        itemService.addItem(user1.getId(), itemDto1);

        TypedQuery<Item> query = em.createQuery("Select it from Item it where it.owner.id = :ownerId", Item.class);
        Item item = query.setParameter("ownerId", user1.getId())
                .getSingleResult();

        assertThat(item, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(itemDto1.getName())),
                hasProperty("description", equalTo(itemDto1.getDescription())),
                hasProperty("available", equalTo(itemDto1.getAvailable())),
                hasProperty("request", notNullValue())
        ));
    }

    @Test
    void testAddItemNonexistentUser() {
        assertThrows(NotFoundException.class, () -> itemService.addItem(NONEXISTENT_ID, itemDto1));
    }

    @Test
    void testAddItemNonexistentRequest() {
        UserDto user = userService.addUser(userDto1);
        itemDto1.setRequestId(NONEXISTENT_ID);

        assertThrows(NotFoundException.class, () -> itemService.addItem(user.getId(), itemDto1));
    }

    @Test
    void testGetAllUserItems() {
        UserDto user = userService.addUser(userDto1);

        ItemDto item1 = itemService.addItem(user.getId(), itemDto1);
        ItemDto item2 = itemService.addItem(user.getId(), itemDto2);
        List<ItemDto> sourceItems = List.of(item1, item2);

        List<ItemDtoWithBookings> targetItems = itemService.getAllUserItems(user.getId());

        assertThat(targetItems, hasSize(sourceItems.size()));
        for (ItemDto sourceItem : sourceItems) {
            assertThat(targetItems, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("name", equalTo(sourceItem.getName())),
                    hasProperty("description", equalTo(sourceItem.getDescription())),
                    hasProperty("available", equalTo(sourceItem.getAvailable()))
            )));
        }
    }

    @Test
    void testGetItemByIdByOwner() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);

        ItemDto addedItem = itemService.addItem(user1.getId(), itemDto1);

        bookingDto1.setItemId(addedItem.getId());
        bookingDto2.setItemId(addedItem.getId());

        bookingService.addBooking(user2.getId(), bookingDto1);
        bookingService.addBooking(user2.getId(), bookingDto2);

        ItemDtoWithBookings itemDto = itemService.getItemById(user1.getId(), addedItem.getId());

        assertThat(itemDto, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(itemDto1.getName())),
                hasProperty("description", equalTo(itemDto1.getDescription())),
                hasProperty("available", equalTo(itemDto1.getAvailable())),
                hasProperty("lastBooking", notNullValue()),
                hasProperty("nextBooking", notNullValue()))
        );
    }

    @Test
    void testGetItemByIdNotByOwner() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);

        ItemDto addedItem = itemService.addItem(user1.getId(), itemDto1);

        ItemDtoWithBookings item = itemService.getItemById(user2.getId(), addedItem.getId());

        assertThat(item, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(itemDto1.getName())),
                hasProperty("description", equalTo(itemDto1.getDescription())),
                hasProperty("available", equalTo(itemDto1.getAvailable())),
                hasProperty("lastBooking", nullValue()),
                hasProperty("nextBooking", nullValue()))
        );
    }

    @Test
    void testSearchItem() {
        UserDto user = userService.addUser(userDto1);
        itemService.addItem(user.getId(), itemDto1);

        List<ItemDto> items = itemService.searchItem("harp");

        assertThat(items, hasSize(1));
        assertThat(items.getFirst(), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(itemDto1.getName())),
                hasProperty("description", equalTo(itemDto1.getDescription())),
                hasProperty("available", equalTo(itemDto1.getAvailable()))
        ));
    }

    @Test
    void testSearchItemBlank() {
        List<ItemDto> targetItems = itemService.searchItem("");

        assertThat(targetItems, empty());
    }

    @Test
    void testUpdateItem() {
        UserDto user = userService.addUser(userDto1);

        ItemDto addedItem = itemService.addItem(user.getId(), itemDto1);
        ItemSaveDto itemSaveDto = itemDto1;
        itemSaveDto.setDescription("Finest harp");
        ItemDto updatedItem = itemService.updateItem(user.getId(), addedItem.getId(), itemSaveDto);

        assertThat(updatedItem, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("name", equalTo(itemSaveDto.getName())),
                hasProperty("description", equalTo(itemSaveDto.getDescription())),
                hasProperty("available", equalTo(itemSaveDto.getAvailable()))
        ));
    }

    @Test
    void testUpdateItemForbidden() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);

        ItemDto addedItem = itemService.addItem(user1.getId(), itemDto1);
        ItemSaveDto itemSaveDto = itemDto1;
        itemSaveDto.setDescription("Finest harp");
        assertThrows(ForbiddenException.class,
                () -> itemService.updateItem(user2.getId(), addedItem.getId(), itemSaveDto));
    }

    @Test
    void testAddComment() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        ItemDto item = itemService.addItem(user1.getId(), itemDto1);
        bookingDto1.setItemId(item.getId());
        BookingDto addedBooking = bookingService.addBooking(user2.getId(), bookingDto1);
        bookingService.approveBooking(user1.getId(), addedBooking.getId(), true);

        CommentDto addedComment = itemService.addComment(user2.getId(), item.getId(), commentDto);
        assertThat(addedComment, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("text", equalTo(commentDto.getText())),
                hasProperty("authorName", equalTo(user2.getName()))
        ));
    }

    @Test
    void testAddCommentNonexistentBooking() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        ItemDto itemDto = itemService.addItem(user1.getId(), itemDto1);

        assertThrows(BookingUnavailableException.class,
                () -> itemService.addComment(user2.getId(), itemDto.getId(), commentDto));
    }

    @Test
    void testAddCommentUnavailable() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        ItemDto item1 = itemService.addItem(user1.getId(), itemDto1);
        ItemDto item2 = itemService.addItem(user1.getId(), itemDto2);
        bookingDto1.setItemId(item1.getId());
        bookingService.addBooking(user2.getId(), bookingDto1);
        bookingDto2.setItemId(item2.getId());
        BookingDto addedBooking2 = bookingService.addBooking(user2.getId(), bookingDto2);
        bookingService.approveBooking(user1.getId(), addedBooking2.getId(), true);

        assertThrows(BookingUnavailableException.class,
                () -> itemService.addComment(user2.getId(), item1.getId(), commentDto));
        assertThrows(BookingUnavailableException.class,
                () -> itemService.addComment(user2.getId(), item2.getId(), commentDto));
    }
}
