package com.example.demo.application.job;

import com.example.demo.application.job.param.CreateArticlesPartitioningJobParam;
import com.example.demo.domain.entity.ArticleFile;
import com.example.demo.domain.repository.ArticleFileRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@Slf4j
public class CreateArticlesPartitioningJobConfig {

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final CreateArticlesPartitioningJobParam createArticlesPartitioningJobParam;
  private final ArticleFileRepository articleFileRepository;

  public CreateArticlesPartitioningJobConfig(
      JobBuilderFactory jobBuilderFactory,
      StepBuilderFactory stepBuilderFactory,
      CreateArticlesPartitioningJobParam createArticlesPartitioningJobParam,
      ArticleFileRepository articleFileRepository) {
    this.jobBuilderFactory = jobBuilderFactory;
    this.stepBuilderFactory = stepBuilderFactory;
    this.createArticlesPartitioningJobParam = createArticlesPartitioningJobParam;
    this.articleFileRepository = articleFileRepository;
  }

  @Bean
  public TaskExecutorPartitionHandler createArticlesPartitionHandler() {
    final int poolSize = 5;

    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setThreadNamePrefix("partition-thread");
    executor.setCorePoolSize(poolSize);
    executor.setMaxPoolSize(poolSize);
    executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
    executor.initialize();

    TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
    partitionHandler.setStep(this.createArticlesWork());
    partitionHandler.setTaskExecutor(executor);
    partitionHandler.setGridSize(poolSize);
    return partitionHandler;
  }

  @Bean
  public Job createArticlesPartitioningJob() {
    return this.jobBuilderFactory.get("createArticlesPartitioningJob")
        .start(this.createArticlesManagement())
        .build();
  }

  @Bean
  public Step createArticlesManagement() {
    return this.stepBuilderFactory.get("createArticlesManagement")
        .partitioner("createArticlesPartition", this.createArticlesPartitioner())
        .partitionHandler(this.createArticlesPartitionHandler())
        .build();
  }

  @Bean
  @StepScope
  public Partitioner createArticlesPartitioner() {
    return gridSize -> {
      List<ArticleFile> articleFiles = this.articleFileRepository.findAllByCreatedDate(createArticlesPartitioningJobParam.getCreatedDate());
      long min = articleFiles.get(0).getId();
      long max = articleFiles.get(articleFiles.size() - 1).getId();
      long targetSize = (max - min) / gridSize + 1;

      Map<String, ExecutionContext> result = new HashMap<>();
      long number = 0;
      long start = min;
      long end = start + targetSize - 1;

      while (start <= max) {
        ExecutionContext value = new ExecutionContext();
        result.put("partition" + number, value);

        if (end >= max) {
          end = max;
        }

        value.putLong("minValue", start);
        value.putLong("maxValue", end);
        start += targetSize;
        end += targetSize;
        number++;
      }

      return result;
    };
  }

  @Bean
  public Step createArticlesWork() {
    return this.stepBuilderFactory.get("createArticlesWork")
        .tasklet(this.createArticlesTasklet())
        .build();
  }

  @Bean
  @StepScope
  public Tasklet createArticlesTasklet() {
    return (contribution, chunkContext) -> {
      Map<String, Object> stepExecutionContext = chunkContext.getStepContext().getStepExecutionContext();
      long minId = (long) stepExecutionContext.get("minValue");
      long maxId = (long) stepExecutionContext.get("maxValue");
      List<ArticleFile> articleFiles = this.articleFileRepository.findAllByIdBetween(minId, maxId);
      log.info("PARTITIONING!!!!");
      log.info("minId = {}", minId);
      log.info("maxId = {}", maxId);
      for (ArticleFile articleFile: articleFiles) {
        log.info("articleFile = {}", articleFile.toString());
      }
      return RepeatStatus.FINISHED;
    };
  }
}
