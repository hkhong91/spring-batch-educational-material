package com.example.demo.domain.repository;

import com.example.demo.domain.entity.Article;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {

  Page<Article> findAllByCreatedAtBefore(LocalDateTime createdAt, Pageable pageable);

}
