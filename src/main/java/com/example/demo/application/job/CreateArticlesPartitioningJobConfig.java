package com.example.demo.application.job;

import com.example.demo.application.job.param.CreateArticlesJobParam;
import com.example.demo.application.model.ArticleModel;
import com.example.demo.domain.entity.Article;
import com.example.demo.domain.repository.ArticleRepository;
import com.example.demo.util.FileUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Configuration
@Slf4j
public class CreateArticlesPartitioningJobConfig {

  private static final int CHUNK_SIZE = 1000;
  private static final int POOL_SIZE = 5;
  private static final int GRID_SIZE = 10;
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final CreateArticlesJobParam createArticlesJobParam;
  private final JdbcTemplate demoJdbcTemplate;
  private final ArticleRepository articleRepository;

  public CreateArticlesPartitioningJobConfig(JobBuilderFactory jobBuilderFactory,
                                             StepBuilderFactory stepBuilderFactory,
                                             CreateArticlesJobParam createArticlesJobParam,
                                             @Qualifier("demoJdbcTemplate") JdbcTemplate demoJdbcTemplate,
                                             ArticleRepository articleRepository) {
    this.jobBuilderFactory = jobBuilderFactory;
    this.stepBuilderFactory = stepBuilderFactory;
    this.createArticlesJobParam = createArticlesJobParam;
    this.demoJdbcTemplate = demoJdbcTemplate;
    this.articleRepository = articleRepository;
  }

  @Bean
  public TaskExecutorPartitionHandler createArticlesPartitionHandler() throws Exception {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setThreadNamePrefix("partition-thread");
    executor.setCorePoolSize(POOL_SIZE);
    executor.setMaxPoolSize(POOL_SIZE);
    executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
    executor.initialize();

    TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
    partitionHandler.setStep(this.createArticlesWork());
    partitionHandler.setTaskExecutor(executor);
    partitionHandler.setGridSize(GRID_SIZE);
    return partitionHandler;
  }

  @Bean
  public Job createArticlesPartitioningJob() throws Exception {
    return this.jobBuilderFactory.get("createArticlesPartitioningJob")
        .start(this.createArticlesManagement())
        .build();
  }

  @Bean
  public Step createArticlesManagement() throws Exception {
    return this.stepBuilderFactory.get("createArticlesManagement")
        .partitioner("createArticlesPartition", this.createArticlesPartitioner())
        .partitionHandler(this.createArticlesPartitionHandler())
        .build();
  }

  @SneakyThrows
  @Bean
  @StepScope
  public Partitioner createArticlesPartitioner() {
    MultiResourcePartitioner partitioner = new MultiResourcePartitioner();
    Resource[] resources =
        FileUtils
            .stream(Paths.get("/Users/Hong/Storage", createArticlesJobParam.getCreatedDate()))
            .filter(File::isFile)
            .filter(file -> "csv".equals(StringUtils.getFilenameExtension(file.getPath())))
            .map(FileSystemResource::new)
            .toArray(FileSystemResource[]::new);

    partitioner.setResources(resources);
    partitioner.partition(POOL_SIZE);
    return partitioner;
  }

  @Bean
  public Step createArticlesWork() throws Exception {
    return this.stepBuilderFactory.get("createArticlesWork")
        .<ArticleModel, Article>chunk(CHUNK_SIZE)
        .reader(this.createArticlesFilePartitioningReader(null))
        .processor(this.createArticlesProcessor())
        .writer(this.createArticlesWriter())
        .build();
  }

  @Bean
  @StepScope
  public FlatFileItemReader<ArticleModel> createArticlesFilePartitioningReader(@Value("#{stepExecutionContext['fileName']}") final String fileName) throws Exception {
    return new FlatFileItemReaderBuilder<ArticleModel>()
        .name("createArticlesFileReader")
        .delimited()
        .names("title", "content")
        .targetType(ArticleModel.class)
        .fieldSetMapper(new BeanWrapperFieldSetMapper<>())
        .resource(new UrlResource(fileName))
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
    return this.articleRepository::insertAll;
  }
}
