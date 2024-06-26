package ru.practicum.shareit.item.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.CommentShort;
import ru.practicum.shareit.item.model.Item;

import javax.persistence.EntityManager;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CommentRepositoryTest {

    private final CommentRepository commentRepository;
    private final EntityManager em;

    @Test
    public void findAllByItemIdOk() {
        var itemId = em.createQuery("select c from Comment as c", Comment.class)
                .getResultStream()
                .findAny()
                .get().getItem().getId();
        var comments = em.createQuery("select c from Comment as c where c.item.id = :id", Comment.class)
                .setParameter("id", itemId)
                .getResultStream()
                .map(this::convert)
                .collect(Collectors.toList());
        var result = commentRepository.findAllByItemId(itemId);

        assertThat(result).hasSize(comments.size());
        assertThat(result).usingRecursiveComparison().isEqualTo(comments);
    }

    @Test
    public void findAllByItemIdInOk() {
        var itemId = em.createQuery("select it from Item as it", Item.class)
                .getResultStream()
                .map(Item::getId)
                .collect(Collectors.toList());
        var comments = em.createQuery("select c from Comment as c where c.item.id in :id", Comment.class)
                .setParameter("id", itemId)
                .getResultStream()
                .map(this::convert)
                .collect(Collectors.toList());
        var result = commentRepository.findAllByItemIdIn(itemId);

        assertThat(result).hasSize(comments.size());
        assertThat(result).usingRecursiveComparison().isEqualTo(comments);
    }

    private CommentShort convert(Comment comment) {
        if (comment == null) {
            return null;
        }
        return CommentShort.builder()
                .id(comment.getId())
                .itemId(comment.getItem() == null ? null : comment.getItem().getId())
                .authorName(comment.getAuthor() == null ? null : comment.getAuthor().getName())
                .text(comment.getText())
                .created(comment.getCreated())
                .build();
    }

}