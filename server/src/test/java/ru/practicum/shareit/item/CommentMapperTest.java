package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CommentMapperTest {
    private final CommentMapper commentMapper;

    @Test
    void testMapToCommentDto() {
        User author = new User();
        author.setName("Alice");

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("It's comment");
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        CommentDto commentDto = commentMapper.mapToCommentDto(comment);

        assertThat(commentDto, allOf(
                hasProperty("id", equalTo(comment.getId())),
                hasProperty("text", equalTo(comment.getText())),
                hasProperty("authorName", equalTo(author.getName())),
                hasProperty("created", equalTo(comment.getCreated()))
        ));
    }
}
