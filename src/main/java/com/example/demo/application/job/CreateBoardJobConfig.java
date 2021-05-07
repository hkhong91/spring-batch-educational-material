package com.example.demo.application.job;

import com.example.demo.application.model.BoardModel;
import com.example.demo.domain.entity.Board;
import com.example.demo.util.FileUtils;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Configuration
@Slf4j
public class CreateBoardJobConfig {

  private static final int CHUNK_SIZE = 1000;
  private static final int POOL_SIZE = 5;
  private static final int GRID_SIZE = 10;
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final JdbcTemplate demoJdbcTemplate;

  public CreateBoardJobConfig(JobBuilderFactory jobBuilderFactory,
                              StepBuilderFactory stepBuilderFactory,
                              @Qualifier("demoJdbcTemplate") JdbcTemplate demoJdbcTemplate) {
    this.jobBuilderFactory = jobBuilderFactory;
    this.stepBuilderFactory = stepBuilderFactory;
    this.demoJdbcTemplate = demoJdbcTemplate;
  }

  @Bean
  public Job createBoardJob() throws Exception {
    return jobBuilderFactory.get("createBoardJob")
        .start(createBoardManager())
        .build();
  }

  @Bean
  public Step createBoardManager() throws Exception {
    return stepBuilderFactory.get("createBoardManager")
        .partitioner("createBoardPartitioner", createBoardPartitioner())
        .partitionHandler(createBoardPartitionHandler())
        .build();
  }

  @Bean
  @StepScope
  public Partitioner createBoardPartitioner() {
    MultiResourcePartitioner partitioner = new MultiResourcePartitioner();

    Path path = Paths.get("/Users/Hong/Storage");
    Resource[] resources = FileUtils.stream(path)
        .filter(File::isFile)
        .filter(file -> "csv".equals(StringUtils.getFilenameExtension(file.getPath())))
        .map(FileSystemResource::new)
        .toArray(Resource[]::new);
    partitioner.setResources(resources);
    partitioner.partition(GRID_SIZE);
    return partitioner;
  }

  public ThreadPoolTaskExecutor createBoardTaskExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(POOL_SIZE);
    taskExecutor.setMaxPoolSize(POOL_SIZE);
    taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
    taskExecutor.initialize();
    return taskExecutor;
  }

  public TaskExecutorPartitionHandler createBoardPartitionHandler() throws Exception {
    TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
    partitionHandler.setStep(createBoardWorker());
    partitionHandler.setGridSize(GRID_SIZE);
    partitionHandler.setTaskExecutor(createBoardTaskExecutor());
    return partitionHandler;
  }

  @Bean
  public Step createBoardWorker() throws Exception {
    return stepBuilderFactory.get("createBoardWorker")
        .<BoardModel, Board>chunk(CHUNK_SIZE)
        .reader(createBoardReader(null))
        .processor(createBoardProcessor())
        .writer(createBoardWriter())
        .build();
  }

  @Bean
  @StepScope
  public FlatFileItemReader<BoardModel> createBoardReader(@Value("#{stepExecutionContext[fileName]}") String fileName) throws Exception {
    return new FlatFileItemReaderBuilder<BoardModel>()
        .name("createBoardReader")
        .resource(new UrlResource(fileName))
        .delimited()
        .names("title", "content")
        .fieldSetMapper(new BeanWrapperFieldSetMapper<>())
        .targetType(BoardModel.class)
        .build();
  }

  public ItemProcessor<BoardModel, Board> createBoardProcessor() {
    LocalDateTime now = LocalDateTime.now();
    return boardModel -> Board.builder()
        .title(boardModel.getTitle())
        .content(boardModel.getContent())
        .createdAt(now)
        .build();
  }

  public ItemWriter<Board> createBoardWriter() {
    return boards -> demoJdbcTemplate.batchUpdate("insert into Board (title, content, createdAt) values (?, ?, ?)",
        boards,
        CHUNK_SIZE,
        (ps, board) -> {
          ps.setObject(1, board.getTitle());
          ps.setObject(2, board.getContent());
          ps.setObject(3, board.getCreatedAt());
        });
  }
}
