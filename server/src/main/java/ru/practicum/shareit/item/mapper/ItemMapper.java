package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookings;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemSaveDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    ItemDto mapToItemDto(Item item);

    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "lastBooking", source = "lastBooking.start")
    @Mapping(target = "nextBooking", source = "nextBooking.start")
    @Mapping(target = "comments", source = "comments")
    ItemDtoWithBookings mapToItemDtoWithBookings(Item item, Booking lastBooking, Booking nextBooking,
                                                 List<Comment> comments);

    Item mapToItem(ItemSaveDto itemSaveDto);

    @Mapping(target = "itemId", source = "id")
    @Mapping(target = "ownerId", source = "owner.id")
    ItemResponseDto mapToResponseDto(Item item);

    List<ItemResponseDto> mapToItemResponseDto(List<Item> items);
}
