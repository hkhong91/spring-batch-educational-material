package com.example.demo.application.job;

import com.example.demo.application.model.ArticleModel;
import com.example.demo.domain.entity.Article;
import com.example.demo.domain.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class CreateArticlesJobConfig {

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final ArticleRepository articleRepository;

  @Bean
  public Job createArticlesJob() {
    log.info("JOB!!!!!");
    return jobBuilderFactory.get("createArticlesJob")
        .incrementer(new RunIdIncrementer())
        .start(this.createArticlesStep())
        .build();
  }

  @Bean
  public Step createArticlesStep() {
    log.info("STEP!!!!!");
    return stepBuilderFactory.get("createArticlesStep")
        .<ArticleModel, Article>chunk(10)
        .reader(this.articlesFileReader())
        .processor(this.articlesProcessor())
        .writer(this.articlesWriter())
        .build();
  }

  public FlatFileItemReader<ArticleModel> articlesFileReader() {
    log.info("READ!!!!!");
    return new FlatFileItemReaderBuilder<ArticleModel>()
        .name("articlesFileReader")
        .resource(new ClassPathResource("Articles.csv"))
        .delimited()
        .names("title", "content")
        .targetType(ArticleModel.class)
        .fieldSetMapper(new BeanWrapperFieldSetMapper<>())
        .build();
  }

  public ItemProcessor<ArticleModel, Article> articlesProcessor() {
    log.info("PROCESS!!!!!");
    return articleModel -> Article.builder()
        .title(articleModel.getTitle())
        .content(articleModel.getContent())
        .build();
  }

  public RepositoryItemWriter<Article> articlesWriter() {
    log.info("WRITE!!!!!");
    return new RepositoryItemWriterBuilder<Article>()
        .repository(this.articleRepository)
        .build();
  }

}
