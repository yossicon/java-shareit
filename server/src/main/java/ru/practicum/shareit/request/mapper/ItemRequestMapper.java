package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;
import ru.practicum.shareit.request.model.ItemRequest;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {

    ItemRequestDto mapToItemRequestDto(ItemRequest request);

    ItemRequest mapToItemRequest(ItemRequestSaveDto requestDto);
}
