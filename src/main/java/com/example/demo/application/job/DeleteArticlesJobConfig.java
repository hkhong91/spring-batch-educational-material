package com.example.demo.application.job;

import com.example.demo.domain.entity.Article;
import com.example.demo.domain.repository.ArticleRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort.Direction;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class DeleteArticlesJobConfig {

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final ArticleRepository articleRepository;

  @Bean
  public Job deleteArticlesJob() {
    return this.jobBuilderFactory.get("deleteArticlesJob")
        .incrementer(new RunIdIncrementer())
        .start(this.deleteArticlesStep())
        .build();
  }

  @Bean
  @JobScope
  public Step deleteArticlesStep() {
    return this.stepBuilderFactory.get("deleteArticlesStep")
        .<Article, Article>chunk(10)
        .reader(this.articlesReader(null))
        .processor(this.articlesProcessor())
        .writer(this.articlesWriter())
        .build();
  }

  @Bean
  @StepScope
  public RepositoryItemReader<Article> articlesReader(
      @Value("#{jobParameters[craetedDate]}") String createdDate) {
    log.info("날짜: {}", createdDate);
    LocalDateTime createdAt = LocalDateTime.of(LocalDate.parse(createdDate), LocalTime.MIN);
    return new RepositoryItemReaderBuilder<Article>()
        .name("articlesReader")
        .repository(this.articleRepository)
        .methodName("findAllByCreatedAtBefore")
        .arguments(createdAt)
        .pageSize(10)
        .sorts(Map.of("id", Direction.ASC))
        .build();
  }

  public ItemProcessor<Article, Article> articlesProcessor() {
    return article -> {
      article.setDeleted(true);
      return article;
    };
  }

  public RepositoryItemWriter<Article> articlesWriter() {
    return new RepositoryItemWriterBuilder<Article>()
        .repository(this.articleRepository)
        .build();
  }
}
