package com.example.demo.infrastructure;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Configuration
public class QuerydslConfig {

  @PersistenceContext
  private EntityManager demoEntityManager;

  @Bean
  public JPAQueryFactory demoJpaQueryFactory() {
    return new JPAQueryFactory(this.demoEntityManager);
  }
}
