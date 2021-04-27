package com.example.demo.domain.repository;

import com.example.demo.domain.entity.DeletedArticle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeletedArticleRepository extends JpaRepository<DeletedArticle, Long>, DeletedArticleRepositoryCustom {
}
