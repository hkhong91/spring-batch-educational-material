package com.example.demo.application.job;

import com.example.demo.application.job.param.UpdateArticlesJobParam;
import com.example.demo.domain.entity.Article;
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
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.Map;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class UpdateArticlesJobConfig {

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final UpdateArticlesJobParam updateArticlesJobParam;
  private final EntityManagerFactory demoEntityManagerFactory;

  @Bean
  public Job updateArticlesJob() {
    return this.jobBuilderFactory.get("updateArticlesJob")
        .incrementer(new UniqueRunIdIncrementer())
        .start(this.updateArticlesStep())
        .build();
  }

  @Bean
  @JobScope
  public Step updateArticlesStep() {
    return this.stepBuilderFactory.get("updateArticlesStep")
        .<Article, Article>chunk(10)
        .reader(this.updateArticlesReader())
        .processor(this.updateArticlesProcessor())
        .writer(this.updateArticlesWriter())
        .build();
  }

  @Bean
  @StepScope
  public JpaPagingItemReader<Article> updateArticlesReader() {
    return new JpaPagingItemReaderBuilder<Article>()
        .name("updateArticlesReader")
        .entityManagerFactory(this.demoEntityManagerFactory)
        .queryString("select a from Article a where a.createdAt < :createdAt")
        .parameterValues(Map.of("createdAt", updateArticlesJobParam.getCreatedAt()))
        .pageSize(100)
        .build();
  }

  public ItemProcessor<Article, Article> updateArticlesProcessor() {
    return article -> {
      article.setContent("컨텐츠 수정");
      return article;
    };
  }

  public JpaItemWriter<Article> updateArticlesWriter() {
    return new JpaItemWriterBuilder<Article>()
        .entityManagerFactory(this.demoEntityManagerFactory)
        .build();
  }
}
