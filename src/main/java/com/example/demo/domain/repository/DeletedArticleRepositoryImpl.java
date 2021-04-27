package com.example.demo.domain.repository;

import com.example.demo.domain.entity.DeletedArticle;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

public class DeletedArticleRepositoryImpl implements DeletedArticleRepositoryCustom {

  @PersistenceContext
  private EntityManager demoEntityManager;

  @Transactional(transactionManager = "demoTransactionManager")
  @Override
  public DeletedArticle insert(DeletedArticle deletedArticle) {
    demoEntityManager.persist(deletedArticle);
    return deletedArticle;
  }

  @Transactional(transactionManager = "demoTransactionManager")
  @Override
  public List<DeletedArticle> insertAll(List<? extends DeletedArticle> deletedArticles) {
    List<DeletedArticle> result = new ArrayList<>();
    for (DeletedArticle deletedArticle : deletedArticles) {
      result.add(this.insert(deletedArticle));
    }
    return result;
  }
}
