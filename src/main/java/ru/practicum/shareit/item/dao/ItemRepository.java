package ru.practicum.shareit.item.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select it from Item as it" +
            " where it.available = true" +
            " and (lower(it.name) like lower(concat('%', ?1,'%'))" +
            " or lower(it.description) like lower(concat('%', ?1,'%')))")
    List<Item> search(String text, Pageable pageable);

    List<Item> findAllByOwnerId(Long userId, Pageable pageable);

    @EntityGraph("item-graph")
    List<Item> findAllByRequestId(long requestId);

    @EntityGraph("item-graph")
    List<Item> findAllByRequestIdIn(List<Long> requests);
}
