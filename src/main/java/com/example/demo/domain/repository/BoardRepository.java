package com.example.demo.domain.repository;

import com.example.demo.domain.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface BoardRepository extends JpaRepository<Board, Long> {

  Page<Board> findAllByIdBetween(long minId, long maxId, Pageable pageable);

  Page<Board> findAllByCreatedAtBefore(LocalDateTime createdAt, Pageable pageable);

  @Transactional
  void deleteAllByIdIn(Iterable<Long> ids);
}
