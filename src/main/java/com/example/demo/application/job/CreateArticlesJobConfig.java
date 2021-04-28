package com.example.demo.application.job;

import com.example.demo.application.job.param.CreateArticlesJobParam;
import com.example.demo.application.model.ArticleModel;
import com.example.demo.domain.entity.Article;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Slf4j
public class CreateArticlesJobConfig {

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final CreateArticlesJobParam createArticlesJobParam;
  private final JdbcTemplate demoJdbcTemplate;

  private final int chunkSize = 1000;

  public CreateArticlesJobConfig(JobBuilderFactory jobBuilderFactory,
                                 StepBuilderFactory stepBuilderFactory,
                                 CreateArticlesJobParam createArticlesJobParam,
                                 @Qualifier("demoJdbcTemplate") JdbcTemplate demoJdbcTemplate) {
    this.jobBuilderFactory = jobBuilderFactory;
    this.stepBuilderFactory = stepBuilderFactory;
    this.createArticlesJobParam = createArticlesJobParam;
    this.demoJdbcTemplate = demoJdbcTemplate;
  }

  @Bean
  public Job createArticlesJob() {
    return this.jobBuilderFactory.get("createArticlesJob")
        .start(this.createArticlesStep())
        .build();
  }

  @Bean
  @JobScope
  public Step createArticlesStep() {
    return this.stepBuilderFactory.get("createArticlesStep")
        .<ArticleModel, Article>chunk(this.chunkSize)
        .reader(this.createArticlesFileReader())
        .processor(this.createArticlesProcessor())
        .writer(this.createArticlesWriter())
        .build();
  }

  @Bean
  @StepScope
  public FlatFileItemReader<ArticleModel> createArticlesFileReader() {
    return new FlatFileItemReaderBuilder<ArticleModel>()
        .name("createArticlesFileReader")
        .resource(new ClassPathResource(createArticlesJobParam.getFileName()))
        .delimited()
        .names("title", "content")
        .targetType(ArticleModel.class)
        .fieldSetMapper(new BeanWrapperFieldSetMapper<>())
        .build();
  }

  public ItemProcessor<ArticleModel, Article> createArticlesProcessor() {
    LocalDateTime now = LocalDateTime.now();
    return articleModel -> Article.builder()
        .title(articleModel.getTitle())
        .content(articleModel.getContent())
        .createdAt(now)
        .updatedAt(now)
        .build();
  }

  public ItemWriter<Article> createArticlesWriter() {
    return articles -> demoJdbcTemplate.batchUpdate(
        "insert into Article (title, content, createdAt, updatedAt) values (?, ?, ?, ?)",
        articles,
        this.chunkSize,
        (ps, article) -> {
          ps.setObject(1, article.getTitle());
          ps.setObject(2, article.getContent());
          ps.setObject(3, article.getCreatedAt());
          ps.setObject(4, article.getUpdatedAt());
        });
  }
}
