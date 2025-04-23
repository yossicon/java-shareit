package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.util.HttpHeaderUtil;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestControllerTest {
    private final ObjectMapper mapper;
    @MockBean
    private final ItemRequestService requestService;
    private final MockMvc mvc;
    private ItemRequestDto requestDto;

    @BeforeEach
    public void setUp() {
        requestDto = new ItemRequestDto();
        requestDto.setId(1L);
        requestDto.setDescription("I want to book a bicycle");
        requestDto.setRequester(new UserDto(1L, "Alice", "email"));
        requestDto.setCreated(LocalDateTime.now());
    }

    @Test
    void addRequest() throws Exception {
        when(requestService.addRequest(anyLong(), any(ItemRequestSaveDto.class)))
                .thenReturn(requestDto);

        mvc.perform(post("/requests")
                        .header(HttpHeaderUtil.USER_ID_HEADER, requestDto.getRequester().getId())
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().json(mapper.writeValueAsString(requestDto)));

        verify(requestService, times(1)).addRequest(anyLong(), any(ItemRequestSaveDto.class));
    }

    @Test
    void testGetAllUserRequests() throws Exception {
        List<ItemRequestDto> requests = List.of(requestDto);

        when(requestService.getAllUserRequests(anyLong()))
                .thenReturn(requests);

        mvc.perform(get("/requests")
                        .header(HttpHeaderUtil.USER_ID_HEADER, requestDto.getRequester().getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(requests)));

        verify(requestService, times(1)).getAllUserRequests(anyLong());
    }

    @Test
    void testGetAllRequests() throws Exception {
        List<ItemRequestDto> requests = List.of(requestDto);

        when(requestService.getAllRequests(anyLong()))
                .thenReturn(requests);

        mvc.perform(get("/requests/all")
                        .header(HttpHeaderUtil.USER_ID_HEADER, 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(requests)));

        verify(requestService, times(1)).getAllRequests(anyLong());
    }

    @Test
    void testGetRequestById() throws Exception {
        when(requestService.getRequestById(anyLong()))
                .thenReturn(requestDto);

        mvc.perform(get("/requests/{requestId}", requestDto.getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(requestDto)));

        verify(requestService, times(1)).getRequestById(anyLong());
    }
}
