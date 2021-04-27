package com.example.demo.domain.repository;

import com.example.demo.domain.entity.Article;

import java.util.List;

public interface ArticleRepositoryCustom {

  Article insert(Article Article);

  List<Article> insertAll(List<? extends Article> Articles);
}
