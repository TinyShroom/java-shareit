package ru.practicum.shareit.request.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByUserId(long userId, Sort sort);

    List<Request> findAllByUserIdNot(long userId, Sort sort);

    List<Request> findAllByUserIdNot(long userId, Pageable pageable);
}
