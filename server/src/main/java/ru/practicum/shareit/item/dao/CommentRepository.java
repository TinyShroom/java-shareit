package ru.practicum.shareit.item.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.CommentShort;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select new ru.practicum.shareit.item.model.CommentShort(" +
            "c.id, c.item.id, c.author.name, c.text, c.created) " +
            "from Comment c " +
            "where c.item.id in ?1")
    List<CommentShort> findAllByItemIdIn(List<Long> items);

    @Query("select new ru.practicum.shareit.item.model.CommentShort(" +
            "c.id, c.item.id, c.author.name, c.text, c.created) " +
            "from Comment c " +
            "where c.item.id = ?1")
    List<CommentShort> findAllByItemId(long itemId);
}
