package com.example.demo.domain.repository;

import com.example.demo.domain.entity.ArticleFile;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleFileRepository extends JpaRepository<ArticleFile, Long> {

  List<ArticleFile> findAllByCreatedDate(LocalDate createdDate);

  List<ArticleFile> findAllByIdBetween(long minId, long maxId);
}
