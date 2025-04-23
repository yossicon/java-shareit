package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookings;
import ru.practicum.shareit.item.dto.ItemSaveDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemMapperTest {
    private final ItemMapper itemMapper;

    @Test
    void testMapToItemDto() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Harp");
        item.setDescription("Fine harp");
        item.setAvailable(true);

        ItemDto itemDto = itemMapper.mapToItemDto(item);

        assertThat(itemDto, allOf(
                hasProperty("id", equalTo(item.getId())),
                hasProperty("name", equalTo(item.getName())),
                hasProperty("description", equalTo(item.getDescription())),
                hasProperty("available", equalTo(item.getAvailable()))
        ));
    }

    @Test
    void testMapToItemDtoWithBookings() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Harp");
        item.setDescription("Fine harp");
        item.setAvailable(true);

        Booking booking1 = new Booking();
        booking1.setStart(LocalDateTime.of(2025, 2, 5, 15, 0));

        Booking booking2 = new Booking();
        booking1.setStart(LocalDateTime.of(2025, 5, 5, 15, 0));

        ItemDtoWithBookings itemDtoWithBookings = itemMapper.mapToItemDtoWithBookings(
                item,
                booking1,
                booking2,
                List.of()
        );

        assertThat(itemDtoWithBookings, allOf(
                hasProperty("id", equalTo(item.getId())),
                hasProperty("name", equalTo(item.getName())),
                hasProperty("description", equalTo(item.getDescription())),
                hasProperty("available", equalTo(item.getAvailable())),
                hasProperty("lastBooking", equalTo(booking1.getStart())),
                hasProperty("nextBooking", equalTo(booking2.getStart())),
                hasProperty("comments", empty())
        ));
    }

    @Test
    void testMapToItem() {
        ItemSaveDto itemSaveDto = new ItemSaveDto();
        itemSaveDto.setName("Harp");
        itemSaveDto.setDescription("Fine harp");
        itemSaveDto.setAvailable(true);

        Item item = itemMapper.mapToItem(itemSaveDto);

        assertThat(item, allOf(
                hasProperty("name", equalTo(itemSaveDto.getName())),
                hasProperty("description", equalTo(itemSaveDto.getDescription())),
                hasProperty("available", equalTo(itemSaveDto.getAvailable()))
        ));
    }
}
