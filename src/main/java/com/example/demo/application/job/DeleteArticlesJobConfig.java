package com.example.demo.application.job;

import com.example.demo.application.job.param.DeleteArticlesJobParam;
import com.example.demo.domain.entity.Article;
import com.example.demo.util.UniqueRunIdIncrementer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@Slf4j
public class DeleteArticlesJobConfig {

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final DeleteArticlesJobParam deleteArticlesJobParam;
  private final DataSource batchDataSource;
  private final DataSource demoDataSource;

  public DeleteArticlesJobConfig(JobBuilderFactory jobBuilderFactory,
                                 StepBuilderFactory stepBuilderFactory,
                                 DeleteArticlesJobParam deleteArticlesJobParam,
                                 @Qualifier("batchDataSource") DataSource batchDataSource,
                                 @Qualifier("demoDataSource") DataSource demoDataSource) {
    this.jobBuilderFactory = jobBuilderFactory;
    this.stepBuilderFactory = stepBuilderFactory;
    this.deleteArticlesJobParam = deleteArticlesJobParam;
    this.batchDataSource = batchDataSource;
    this.demoDataSource = demoDataSource;
  }

  @Bean
  public Job deleteArticlesJob() {
    return this.jobBuilderFactory.get("deleteArticlesJob")
        .incrementer(new UniqueRunIdIncrementer())
        .start(this.deleteArticlesStep())
        .build();
  }

  @Bean
  @JobScope
  public Step deleteArticlesStep() {
    return this.stepBuilderFactory.get("deleteArticlesStep")
        .<Article, Article>chunk(10)
        .reader(this.deleteArticlesReader())
        .processor(this.deleteArticlesProcessor())
        .writer(this.deleteArticlesWriter())
        .build();
  }

  @Bean
  @StepScope
  public JdbcPagingItemReader<Article> deleteArticlesReader() {
    return new JdbcPagingItemReaderBuilder<Article>()
        .name("deleteArticlesReader")
        .dataSource(this.demoDataSource)
        .selectClause("id, title, content, isDeleted, createdAt, updatedAt")
        .fromClause("from Article")
        .whereClause("where createdAt < :createdAt")
        .parameterValues(Map.of("createdAt", this.deleteArticlesJobParam.getCreatedAt()))
        .rowMapper(new BeanPropertyRowMapper<>(Article.class))
        .sortKeys(Map.of("id", Order.ASCENDING))
        .pageSize(10)
        .build();
  }

  public ItemProcessor<Article, Article> deleteArticlesProcessor() {
    return article -> {
      article.setDeleted(false);
      return article;
    };
  }

  public JdbcBatchItemWriter<Article> deleteArticlesWriter() {
    return new JdbcBatchItemWriterBuilder<Article>()
        .dataSource(this.demoDataSource)
        .sql("update Article set isDeleted = ? where id = ?")
        .itemPreparedStatementSetter((article, ps) -> {
          ps.setBoolean(1, article.isDeleted());
          ps.setLong(2, article.getId());
        })
        .build();
  }
}
