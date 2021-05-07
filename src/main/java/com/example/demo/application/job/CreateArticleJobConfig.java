package com.example.demo.application.job;

import com.example.demo.application.job.param.CreateArticleJobParam;
import com.example.demo.application.model.ArticleModel;
import com.example.demo.domain.entity.Article;
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
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

@Configuration
@Slf4j
public class CreateArticleJobConfig {

  private static final int CHUNK_SIZE = 1000;
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final CreateArticleJobParam createArticleJobParam;
  private final JdbcTemplate demoJdbcTemplate;

  public CreateArticleJobConfig(JobBuilderFactory jobBuilderFactory,
                                StepBuilderFactory stepBuilderFactory,
                                CreateArticleJobParam createArticleJobParam,
                                @Qualifier("demoJdbcTemplate") JdbcTemplate demoJdbcTemplate) {
    this.jobBuilderFactory = jobBuilderFactory;
    this.stepBuilderFactory = stepBuilderFactory;
    this.createArticleJobParam = createArticleJobParam;
    this.demoJdbcTemplate = demoJdbcTemplate;
  }

  @Bean
  public Job createArticleJob() {
    return jobBuilderFactory.get("createArticleJob")
        .start(createArticleStep())
        .build();
  }

  @Bean
  @JobScope
  public Step createArticleStep() {
    return stepBuilderFactory.get("createArticleStep")
        .<ArticleModel, Article>chunk(CHUNK_SIZE)
        .reader(createArticleReader())
        .processor(createArticleProcessor())
        .writer(createArticleWriter())
        .faultTolerant()
        .skipLimit(3)
        .skip(FlatFileParseException.class)
        .build();
  }

  @Bean
  @StepScope
  public FlatFileItemReader<ArticleModel> createArticleReader() {
    return new FlatFileItemReaderBuilder<ArticleModel>()
        .name("createArticleReader")
        .resource(new ClassPathResource(createArticleJobParam.getName()))
        .delimited()
        .names("title", "content")
        .fieldSetMapper(new BeanWrapperFieldSetMapper<>())
        .targetType(ArticleModel.class)
        .build();
  }

  public ItemProcessor<ArticleModel, Article> createArticleProcessor() {
    LocalDateTime now = LocalDateTime.now();
    return articleModel -> Article.builder()
        .title(articleModel.getTitle())
        .content(articleModel.getContent())
        .createdAt(now)
        .build();
  }

  public ItemWriter<Article> createArticleWriter() {
    return articles -> demoJdbcTemplate.batchUpdate("insert into Article (title, content, createdAt) values (?, ?, ?)",
        articles,
        CHUNK_SIZE,
        (ps, article) -> {
          ps.setObject(1, article.getTitle());
          ps.setObject(2, article.getContent());
          ps.setObject(3, article.getCreatedAt());
        });
  }
}
