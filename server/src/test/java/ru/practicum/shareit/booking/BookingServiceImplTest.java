package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ItemUnavailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemSaveDto;
import ru.practicum.shareit.item.service.ItemService;
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
public class BookingServiceImplTest {
    private static final Long NONEXISTENT_ID = 100L;

    private final EntityManager em;
    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;

    private UserSaveDto userDto1;
    private UserSaveDto userDto2;
    private ItemSaveDto itemDto;
    private BookingSaveDto bookingDto;

    @BeforeEach
    public void setUp() {
        userDto1 = new UserSaveDto("Floyd", "wrupnk@gmail.com");
        userDto2 = new UserSaveDto("Alice", "ainchns@gmail.com");

        itemDto = new ItemSaveDto("Harp", "Fine harp", true, null);

        bookingDto = new BookingSaveDto();
        bookingDto.setStart(LocalDateTime.of(2025, 5, 5, 15, 0));
        bookingDto.setEnd(LocalDateTime.of(2025, 5, 25, 15, 0));
    }

    @Test
    void testAddBooking() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        ItemDto item = itemService.addItem(user1.getId(), itemDto);

        bookingDto.setItemId(item.getId());
        bookingService.addBooking(user2.getId(), bookingDto);

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b where b.item.id = :itemId", Booking.class);
        Booking booking = query.setParameter("itemId", item.getId())
                .getSingleResult();

        assertThat(booking, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(bookingDto.getStart())),
                hasProperty("end", equalTo(bookingDto.getEnd())),
                hasProperty("status", equalTo(BookingStatus.WAITING))
        ));
    }

    @Test
    void testBookUnavailableItem() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        itemDto.setAvailable(false);
        ItemDto itemDto = itemService.addItem(user1.getId(), this.itemDto);

        bookingDto.setItemId(itemDto.getId());

        assertThrows(ItemUnavailableException.class, () -> bookingService.addBooking(user2.getId(), bookingDto));
    }

    @Test
    void testBookNonexistentItem() {
        UserDto user = userService.addUser(userDto2);

        bookingDto.setItemId(NONEXISTENT_ID);

        assertThrows(NotFoundException.class, () -> bookingService.addBooking(user.getId(), bookingDto));
    }

    @Test
    void testGetBookingById() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        ItemDto item = itemService.addItem(user1.getId(), itemDto);

        bookingDto.setItemId(item.getId());
        BookingDto addedBooking = bookingService.addBooking(user2.getId(), bookingDto);

        BookingDto bookingDto = bookingService.getBookingById(user2.getId(), addedBooking.getId());

        assertThat(bookingDto, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(this.bookingDto.getStart())),
                hasProperty("end", equalTo(this.bookingDto.getEnd())),
                hasProperty("status", equalTo(BookingStatus.WAITING))
        ));
    }

    @Test
    void testGetNonexistentBooking() {
        UserDto user = userService.addUser(userDto2);
        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(user.getId(), NONEXISTENT_ID));
    }


    @Test
    void testGetBookingForbidden() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        ItemDto item = itemService.addItem(user1.getId(), itemDto);

        bookingDto.setItemId(item.getId());
        BookingDto addedBooking = bookingService.addBooking(user2.getId(), bookingDto);

        assertThrows(ForbiddenException.class,
                () -> bookingService.getBookingById(NONEXISTENT_ID, addedBooking.getId()));

    }

    @Test
    void testGetAllUserBookings() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        ItemDto item = itemService.addItem(user1.getId(), itemDto);

        bookingDto.setItemId(item.getId());
        BookingDto addedBooking = bookingService.addBooking(user2.getId(), bookingDto);


        List<BookingDto> sourceBookings = List.of(addedBooking);

        List<BookingDto> targetBookings = bookingService.getUserBookings(user2.getId(), BookingState.ALL);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        assertThat(targetBookings.getFirst(), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(bookingDto.getStart())),
                hasProperty("end", equalTo(bookingDto.getEnd())),
                hasProperty("status", equalTo(BookingStatus.WAITING))
        ));
    }

    @Test
    void testGetCurrentUserBookings() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        ItemDto item = itemService.addItem(user1.getId(), itemDto);

        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().minusDays(1));
        BookingDto addedBooking = bookingService.addBooking(user2.getId(), bookingDto);

        List<BookingDto> sourceBookings = List.of(addedBooking);

        List<BookingDto> targetBookings = bookingService.getUserBookings(user2.getId(), BookingState.CURRENT);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        assertThat(targetBookings.getFirst(), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(bookingDto.getStart())),
                hasProperty("end", equalTo(bookingDto.getEnd())),
                hasProperty("status", equalTo(BookingStatus.WAITING))
        ));
    }

    @Test
    void testGetPastUserBookings() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        ItemDto item = itemService.addItem(user1.getId(), itemDto);

        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().minusDays(10));
        bookingDto.setEnd(LocalDateTime.now().minusDays(1));
        BookingDto addedBooking = bookingService.addBooking(user2.getId(), bookingDto);

        List<BookingDto> sourceBookings = List.of(addedBooking);

        List<BookingDto> targetBookings = bookingService.getUserBookings(user2.getId(), BookingState.PAST);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        assertThat(targetBookings.getFirst(), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(bookingDto.getStart())),
                hasProperty("end", equalTo(bookingDto.getEnd())),
                hasProperty("status", equalTo(BookingStatus.WAITING))
        ));
    }

    @Test
    void testGetFutureUserBookings() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        ItemDto item = itemService.addItem(user1.getId(), itemDto);

        bookingDto.setItemId(item.getId());
        BookingDto addedBooking = bookingService.addBooking(user2.getId(), bookingDto);

        List<BookingDto> sourceBookings = List.of(addedBooking);

        List<BookingDto> targetBookings = bookingService.getUserBookings(user2.getId(), BookingState.FUTURE);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        assertThat(targetBookings.getFirst(), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(bookingDto.getStart())),
                hasProperty("end", equalTo(bookingDto.getEnd())),
                hasProperty("status", equalTo(BookingStatus.WAITING))
        ));
    }

    @Test
    void testGetWaitingUserBookings() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        ItemDto item = itemService.addItem(user1.getId(), itemDto);

        bookingDto.setItemId(item.getId());
        BookingDto addedBooking = bookingService.addBooking(user2.getId(), bookingDto);

        List<BookingDto> sourceBookings = List.of(addedBooking);

        List<BookingDto> targetBookings = bookingService.getUserBookings(user2.getId(), BookingState.WAITING);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        assertThat(targetBookings.getFirst(), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(bookingDto.getStart())),
                hasProperty("end", equalTo(bookingDto.getEnd())),
                hasProperty("status", equalTo(BookingStatus.WAITING))
        ));
    }

    @Test
    void testGetRejectedUserBookings() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        ItemDto item = itemService.addItem(user1.getId(), itemDto);

        bookingDto.setItemId(item.getId());
        BookingDto addedBooking = bookingService.addBooking(user2.getId(), bookingDto);

        bookingService.approveBooking(user1.getId(), addedBooking.getId(), false);

        List<BookingDto> sourceBookings = List.of(addedBooking);

        List<BookingDto> targetBookings = bookingService.getUserBookings(user2.getId(), BookingState.REJECTED);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        assertThat(targetBookings.getFirst(), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(bookingDto.getStart())),
                hasProperty("end", equalTo(bookingDto.getEnd())),
                hasProperty("status", equalTo(BookingStatus.REJECTED))
        ));
    }

    @Test
    void testGetAllOwnerBookings() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        ItemDto item = itemService.addItem(user1.getId(), itemDto);

        bookingDto.setItemId(item.getId());
        BookingDto addedBooking = bookingService.addBooking(user2.getId(), bookingDto);

        List<BookingDto> sourceBookings = List.of(addedBooking);

        List<BookingDto> targetBookings = bookingService.getOwnerBookings(user1.getId(), BookingState.ALL);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        assertThat(targetBookings.getFirst(), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(bookingDto.getStart())),
                hasProperty("end", equalTo(bookingDto.getEnd())),
                hasProperty("status", equalTo(BookingStatus.WAITING))
        ));
    }

    @Test
    void testGetCurrentOwnerBookings() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        ItemDto item = itemService.addItem(user1.getId(), itemDto);

        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().minusDays(1));
        BookingDto addedBooking = bookingService.addBooking(user2.getId(), bookingDto);

        List<BookingDto> sourceBookings = List.of(addedBooking);

        List<BookingDto> targetBookings = bookingService.getOwnerBookings(user1.getId(), BookingState.CURRENT);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        assertThat(targetBookings.getFirst(), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(bookingDto.getStart())),
                hasProperty("end", equalTo(bookingDto.getEnd())),
                hasProperty("status", equalTo(BookingStatus.WAITING))
        ));
    }

    @Test
    void testGetPastOwnerBookings() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        ItemDto item = itemService.addItem(user1.getId(), itemDto);

        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().minusDays(10));
        bookingDto.setEnd(LocalDateTime.now().minusDays(1));
        BookingDto addedBooking = bookingService.addBooking(user2.getId(), bookingDto);

        List<BookingDto> sourceBookings = List.of(addedBooking);

        List<BookingDto> targetBookings = bookingService.getOwnerBookings(user1.getId(), BookingState.PAST);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        assertThat(targetBookings.getFirst(), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(bookingDto.getStart())),
                hasProperty("end", equalTo(bookingDto.getEnd())),
                hasProperty("status", equalTo(BookingStatus.WAITING))
        ));
    }

    @Test
    void testGetFutureOwnerBookings() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        ItemDto item = itemService.addItem(user1.getId(), itemDto);

        bookingDto.setItemId(item.getId());
        BookingDto addedBooking = bookingService.addBooking(user2.getId(), bookingDto);

        List<BookingDto> sourceBookings = List.of(addedBooking);

        List<BookingDto> targetBookings = bookingService.getOwnerBookings(user1.getId(), BookingState.FUTURE);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        assertThat(targetBookings.getFirst(), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(bookingDto.getStart())),
                hasProperty("end", equalTo(bookingDto.getEnd())),
                hasProperty("status", equalTo(BookingStatus.WAITING))
        ));
    }

    @Test
    void testGetWaitingOwnerBookings() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        ItemDto item = itemService.addItem(user1.getId(), itemDto);

        bookingDto.setItemId(item.getId());
        BookingDto addedBooking = bookingService.addBooking(user2.getId(), bookingDto);

        List<BookingDto> sourceBookings = List.of(addedBooking);

        List<BookingDto> targetBookings = bookingService.getOwnerBookings(user1.getId(), BookingState.WAITING);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        assertThat(targetBookings.getFirst(), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(bookingDto.getStart())),
                hasProperty("end", equalTo(bookingDto.getEnd())),
                hasProperty("status", equalTo(BookingStatus.WAITING))
        ));
    }

    @Test
    void testGetRejectedOwnerBookings() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        ItemDto item = itemService.addItem(user1.getId(), itemDto);

        bookingDto.setItemId(item.getId());
        BookingDto addedBooking = bookingService.addBooking(user2.getId(), bookingDto);

        bookingService.approveBooking(user1.getId(), addedBooking.getId(), false);

        List<BookingDto> sourceBookings = List.of(addedBooking);

        List<BookingDto> targetBookings = bookingService.getOwnerBookings(user1.getId(), BookingState.REJECTED);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        assertThat(targetBookings.getFirst(), allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(bookingDto.getStart())),
                hasProperty("end", equalTo(bookingDto.getEnd())),
                hasProperty("status", equalTo(BookingStatus.REJECTED))
        ));
    }

    @Test
    void testGetOwnerBookingsWithNoItems() {
        UserDto user = userService.addUser(userDto1);

        assertThrows(NotFoundException.class, () -> bookingService.getOwnerBookings(user.getId(), BookingState.ALL));
    }

    @Test
    void testApproveBooking() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        ItemDto item = itemService.addItem(user1.getId(), itemDto);

        bookingDto.setItemId(item.getId());
        BookingDto addedBooking = bookingService.addBooking(user2.getId(), bookingDto);

        bookingService.approveBooking(user1.getId(), addedBooking.getId(), true);
        BookingDto bookingDto = bookingService.getBookingById(user2.getId(), addedBooking.getId());

        assertThat(bookingDto, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(this.bookingDto.getStart())),
                hasProperty("end", equalTo(this.bookingDto.getEnd())),
                hasProperty("status", equalTo(BookingStatus.APPROVED))
        ));
    }

    @Test
    void testApproveApprovedBooking() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        ItemDto item = itemService.addItem(user1.getId(), itemDto);

        bookingDto.setItemId(item.getId());
        BookingDto addedBooking = bookingService.addBooking(user2.getId(), bookingDto);

        bookingService.approveBooking(user1.getId(), addedBooking.getId(), true);

        assertThrows(DuplicatedDataException.class,
                () -> bookingService.approveBooking(user1.getId(), addedBooking.getId(), true));
    }

    @Test
    void testRejectBooking() {
        UserDto user1 = userService.addUser(userDto1);
        UserDto user2 = userService.addUser(userDto2);
        ItemDto item = itemService.addItem(user1.getId(), itemDto);

        bookingDto.setItemId(item.getId());
        BookingDto addedBooking = bookingService.addBooking(user2.getId(), bookingDto);

        bookingService.approveBooking(user1.getId(), addedBooking.getId(), false);
        BookingDto foundedBooking = bookingService.getBookingById(user2.getId(), addedBooking.getId());

        assertThat(foundedBooking, allOf(
                hasProperty("id", notNullValue()),
                hasProperty("start", equalTo(bookingDto.getStart())),
                hasProperty("end", equalTo(bookingDto.getEnd())),
                hasProperty("status", equalTo(BookingStatus.REJECTED))
        ));
    }

    @Test
    void testApproveForbidden() {
        UserDto owner = userService.addUser(userDto1);
        UserDto booker = userService.addUser(userDto2);
        ItemDto item = itemService.addItem(owner.getId(), itemDto);

        bookingDto.setItemId(item.getId());
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), bookingDto);
        assertThrows(ForbiddenException.class,
                () -> bookingService.approveBooking(NONEXISTENT_ID, addedBooking.getId(), true));
    }
}
