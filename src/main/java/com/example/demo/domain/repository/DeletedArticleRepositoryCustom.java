package com.example.demo.domain.repository;

import com.example.demo.domain.entity.DeletedArticle;

import java.util.List;

public interface DeletedArticleRepositoryCustom {

  DeletedArticle insert(DeletedArticle deletedArticle);

  List<DeletedArticle> insertAll(List<? extends DeletedArticle> deletedArticles);
}
