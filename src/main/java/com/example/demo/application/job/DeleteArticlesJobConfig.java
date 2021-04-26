package com.example.demo.application.job;

import com.example.demo.application.job.param.DeleteArticlesJobParam;
import com.example.demo.domain.entity.Article;
import com.example.demo.domain.repository.ArticleRepository;
import com.example.demo.util.UniqueRunIdIncrementer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort.Direction;

import java.util.Map;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class DeleteArticlesJobConfig {

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final DeleteArticlesJobParam deleteArticlesJobParam;

  private final ArticleRepository articleRepository;

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
  public RepositoryItemReader<Article> deleteArticlesReader() {
    return new RepositoryItemReaderBuilder<Article>()
        .name("deleteArticlesReader")
        .repository(this.articleRepository)
        .methodName("findAllByCreatedAtBefore")
        .arguments(this.deleteArticlesJobParam.getCreatedAt())
        .pageSize(10)
        .sorts(Map.of("id", Direction.ASC))
        .build();
  }

  public ItemProcessor<Article, Article> deleteArticlesProcessor() {
    return article -> {
      article.setDeleted(true);
      return article;
    };
  }

  public RepositoryItemWriter<Article> deleteArticlesWriter() {
    return new RepositoryItemWriterBuilder<Article>()
        .repository(this.articleRepository)
        .build();
  }
}
