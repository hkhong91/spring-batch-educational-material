package com.example.demo.domain.repository;

import com.example.demo.domain.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface ArticleRepository extends JpaRepository<Article, Long>, ArticleRepositoryCustom {

  Page<Article> findAllByCreatedAtBefore(LocalDateTime createdAt, Pageable pageable);

  @Transactional
  void deleteAllByIdIn(Iterable<Long> ids);
}
