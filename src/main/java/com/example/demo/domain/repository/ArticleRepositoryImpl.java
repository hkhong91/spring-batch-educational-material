package com.example.demo.domain.repository;

import com.example.demo.domain.entity.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

  @PersistenceContext
  private EntityManager demoEntityManager;

  @Transactional(transactionManager = "demoTransactionManager")
  @Override
  public Article insert(Article article) {
    demoEntityManager.persist(article);
    return article;
  }

  @Transactional(transactionManager = "demoTransactionManager")
  @Override
  public List<Article> insertAll(List<? extends Article> articles) {
    List<Article> result = new ArrayList<>();
    for (Article article : articles) {
      result.add(this.insert(article));
    }
    return result;
  }
}
