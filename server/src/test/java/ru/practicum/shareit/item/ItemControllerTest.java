package ru.practicum.shareit.item;


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
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.util.HttpHeaderUtil;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemControllerTest {
    private final ObjectMapper mapper;
    @MockBean
    private final ItemService itemService;
    private final MockMvc mvc;
    private ItemDto itemDto;
    private ItemDtoWithBookings itemDtoWithBookings;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Harp");
        itemDto.setDescription("Fine Harp");
        itemDto.setAvailable(true);

        itemDtoWithBookings = new ItemDtoWithBookings();
        itemDtoWithBookings.setId(1L);
        itemDtoWithBookings.setName("Harp");
        itemDtoWithBookings.setDescription("Fine Harp");
        itemDtoWithBookings.setAvailable(true);

        commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("It's comment");
        commentDto.setAuthorName("Alice");
        commentDto.setCreated(LocalDateTime.now());
    }

    @Test
    void testAddItem() throws Exception {
        when(itemService.addItem(anyLong(), any(ItemSaveDto.class)))
                .thenReturn(itemDto);

        mvc.perform(post("/items")
                        .header(HttpHeaderUtil.USER_ID_HEADER, 1)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().json(mapper.writeValueAsString(itemDto)));

        verify(itemService, times(1)).addItem(anyLong(), any(ItemSaveDto.class));
    }

    @Test
    void testAddComment() throws Exception {
        when(itemService.addComment(anyLong(), anyLong(), any(CommentSaveDto.class)))
                .thenReturn(commentDto);

        mvc.perform(post("/items/{itemId}/comment", itemDto.getId())
                        .header(HttpHeaderUtil.USER_ID_HEADER, 1)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().json(mapper.writeValueAsString(commentDto)));

        verify(itemService, times(1)).addComment(anyLong(), anyLong(), any(CommentSaveDto.class));
    }

    @Test
    void testGetAllUserItems() throws Exception {
        List<ItemDtoWithBookings> items = List.of(itemDtoWithBookings);

        when(itemService.getAllUserItems(anyLong()))
                .thenReturn(items);

        mvc.perform(get("/items")
                        .header(HttpHeaderUtil.USER_ID_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(items)));

        verify(itemService, times(1)).getAllUserItems(anyLong());
    }

    @Test
    void testGetItemById() throws Exception {
        when(itemService.getItemById(anyLong(), anyLong()))
                .thenReturn(itemDtoWithBookings);

        mvc.perform(get("/items/{itemId}", itemDtoWithBookings.getId())
                        .header(HttpHeaderUtil.USER_ID_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemDtoWithBookings)));

        verify(itemService, times(1)).getItemById(anyLong(), anyLong());
    }

    @Test
    void testSearchItem() throws Exception {
        List<ItemDto> items = List.of(itemDto);

        when(itemService.searchItem(anyString()))
                .thenReturn(items);

        mvc.perform(get("/items/search")
                        .param("text", itemDto.getName())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(items)));

        verify(itemService, times(1)).searchItem(anyString());
    }

    @Test
    void testUpdateItem() throws Exception {
        when(itemService.updateItem(anyLong(), anyLong(), any(ItemSaveDto.class)))
                .thenReturn(itemDto);

        mvc.perform(patch("/items/{itemId}", itemDto.getId())
                        .header(HttpHeaderUtil.USER_ID_HEADER, 1)
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(itemDto)));

        verify(itemService, times(1)).updateItem(anyLong(), anyLong(), any(ItemSaveDto.class));
    }
}
