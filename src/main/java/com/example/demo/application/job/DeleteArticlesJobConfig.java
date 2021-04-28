package com.example.demo.application.job;

import com.example.demo.application.job.param.DeleteArticlesJobParam;
import com.example.demo.domain.entity.Article;
import com.example.demo.domain.entity.DeletedArticle;
import com.example.demo.domain.repository.ArticleRepository;
import com.example.demo.domain.repository.DeletedArticleRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

@Configuration
@Slf4j
public class DeleteArticlesJobConfig {

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final DeleteArticlesJobParam deleteArticlesJobParam;
  private final ArticleRepository articleRepository;
  private final DeletedArticleRepository deletedArticleRepository;

  public DeleteArticlesJobConfig(JobBuilderFactory jobBuilderFactory,
                                 StepBuilderFactory stepBuilderFactory,
                                 DeleteArticlesJobParam deleteArticlesJobParam,
                                 ArticleRepository articleRepository,
                                 DeletedArticleRepository deletedArticleRepository) {
    this.jobBuilderFactory = jobBuilderFactory;
    this.stepBuilderFactory = stepBuilderFactory;
    this.deleteArticlesJobParam = deleteArticlesJobParam;
    this.articleRepository = articleRepository;
    this.deletedArticleRepository = deletedArticleRepository;
  }

  @Bean
  public Job deleteArticlesJob() {
    return this.jobBuilderFactory.get("deleteArticlesJob")
        .start(this.backupDeletedArticlesStep())
        .next(this.deleteArticlesStep())
        .build();
  }

  @Bean
  @JobScope
  public Step backupDeletedArticlesStep() {
    return this.stepBuilderFactory.get("backupDeletedArticlesStep")
        .<Article, DeletedArticle>chunk(100)
        .reader(this.backupDeletedArticlesReader())
        .processor(this.backupDeletedArticlesProcessor())
        .writer(this.backupDeletedArticlesWriter())
        .listener(this.deleteArticlesPromotionListener())
        .build();
  }

  @Bean
  @JobScope
  public Step deleteArticlesStep() {
    return this.stepBuilderFactory.get("deleteArticlesStep")
        .tasklet((contribution, chunkContext) -> {
          Map<String, Object> jobExecutionContext = chunkContext.getStepContext().getJobExecutionContext();
          List<Long> deletedArticleIds = (List<Long>) jobExecutionContext.get("deletedArticleIds");
          this.articleRepository.deleteAllByIdIn(deletedArticleIds);
          return RepeatStatus.FINISHED;
        })
        .build();
  }

  @Bean
  public ExecutionContextPromotionListener deleteArticlesPromotionListener() {
    ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
    listener.setKeys(new String[]{"deletedArticleIds"});
    return listener;
  }

  @Bean
  @StepScope
  public RepositoryItemReader<Article> backupDeletedArticlesReader() {
    return new RepositoryItemReaderBuilder<Article>()
        .name("backupDeletedArticlesReader")
        .repository(this.articleRepository)
        .methodName("findAllByCreatedAtBefore")
        .arguments(this.deleteArticlesJobParam.getCreatedAt())
        .sorts(Map.of("id", Sort.Direction.ASC))
        .pageSize(100)
        .build();
  }

  public ItemProcessor<Article, DeletedArticle> backupDeletedArticlesProcessor() {
    return article -> DeletedArticle.builder()
        .id(article.getId())
        .title(article.getTitle())
        .content(article.getContent())
        .createdAt(article.getCreatedAt())
        .updatedAt(article.getUpdatedAt())
        .build();
  }

  public ItemWriter<DeletedArticle> backupDeletedArticlesWriter() {
    return new ItemWriter<DeletedArticle>() {
      private StepExecution stepExecution;

      @Override
      public void write(List<? extends DeletedArticle> targetArticles) {
        List<Long> deletedArticleIds = deletedArticleRepository.insertAll(targetArticles)
            .stream()
            .map(DeletedArticle::getId)
            .collect(Collectors.toList());
        ExecutionContext executionContext = this.stepExecution.getJobExecution().getExecutionContext();
        executionContext.put("deletedArticleIds", deletedArticleIds);
      }

      @BeforeStep
      public void saveStepExecution(final StepExecution stepExecution) {
        this.stepExecution = stepExecution;
      }
    };
  }
}
