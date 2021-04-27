package com.example.demo.application.job;

import com.example.demo.application.job.param.CreateArticlesJobParam;
import com.example.demo.application.model.ArticleModel;
import com.example.demo.domain.entity.Article;
import com.example.demo.domain.repository.ArticleRepository;
import com.example.demo.util.UniqueRunIdIncrementer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
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
public class CreateArticlesJobConfig {

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final CreateArticlesJobParam createArticlesJobParam;
  private final ArticleRepository articleRepository;

  public CreateArticlesJobConfig(JobBuilderFactory jobBuilderFactory,
                                 StepBuilderFactory stepBuilderFactory,
                                 CreateArticlesJobParam createArticlesJobParam,
                                 ArticleRepository articleRepository) {
    this.jobBuilderFactory = jobBuilderFactory;
    this.stepBuilderFactory = stepBuilderFactory;
    this.createArticlesJobParam = createArticlesJobParam;
    this.articleRepository = articleRepository;
  }

  @Bean
  public Job createArticlesJob() {
    return this.jobBuilderFactory.get("createArticlesJob")
        .incrementer(new UniqueRunIdIncrementer())
        .start(this.createArticlesStep())
        .build();
  }

  @Bean
  @JobScope
  public Step createArticlesStep() {
    return this.stepBuilderFactory.get("createArticlesStep")
        .<ArticleModel, Article>chunk(10)
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
    return articleModel -> Article.builder()
        .title(articleModel.getTitle())
        .content(articleModel.getContent())
        .build();
  }

  public RepositoryItemWriter<Article> createArticlesWriter() {
    return new RepositoryItemWriterBuilder<Article>()
        .repository(this.articleRepository)
        .build();
  }
}
