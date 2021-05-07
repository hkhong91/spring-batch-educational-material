package com.example.demo.application.job;

import com.example.demo.application.job.param.SoftDeleteBoardJobParam;
import com.example.demo.domain.entity.Board;
import com.example.demo.domain.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.util.Map;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class SoftDeleteBoardJobConfig {

  private static final int CHUNK_SIZE = 1000;
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final SoftDeleteBoardJobParam softDeleteBoardJobParam;
  private final BoardRepository boardRepository;

  @Bean
  public Job softDeleteBoardJob() {
    return jobBuilderFactory.get("softDeleteBoardJob")
        .start(softDeleteBoardStep())
        .build();
  }

  @Bean
  @JobScope
  public Step softDeleteBoardStep() {
    return stepBuilderFactory.get("softDeleteBoardJob")
        .<Board, Board>chunk(CHUNK_SIZE)
        .reader(softDeleteBoardReader())
        .processor(softDeleteBoardProcessor())
        .writer(softDeleteBoardWriter())
        .build();
  }

  @Bean
  @StepScope
  public RepositoryItemReader<Board> softDeleteBoardReader() {
    return new RepositoryItemReaderBuilder<Board>()
        .name("softDeleteBoardReader")
        .repository(boardRepository)
        .methodName("findAllByCreatedAtBefore")
        .arguments(softDeleteBoardJobParam.getCreatedAt())
        .pageSize(CHUNK_SIZE)
        .sorts(Map.of("id", Sort.Direction.ASC))
        .build();
  }

  public ItemProcessor<Board, Board> softDeleteBoardProcessor() {
    return board -> {
      board.setDeleted(true);
      return board;
    };
  }

  public RepositoryItemWriter<Board> softDeleteBoardWriter() {
    return new RepositoryItemWriterBuilder<Board>()
        .repository(boardRepository)
        .build();
  }
}
