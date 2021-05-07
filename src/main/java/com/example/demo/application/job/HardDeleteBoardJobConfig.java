package com.example.demo.application.job;

import com.example.demo.application.job.param.HardDeleteBoardJobParam;
import com.example.demo.domain.entity.Board;
import com.example.demo.domain.entity.DeletedBoard;
import com.example.demo.domain.repository.BoardRepository;
import com.example.demo.domain.repository.DeletedBoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class HardDeleteBoardJobConfig {

  private static final int CHUNK_SIZE = 1000;
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final HardDeleteBoardJobParam hardDeleteBoardJobParam;
  private final BoardRepository boardRepository;
  private final DeletedBoardRepository deletedBoardRepository;

  @Bean
  public Job hardDeleteBoardJob() {
    return jobBuilderFactory.get("hardDeleteBoardJob")
        .start(backupBoardStep())
        .next(hardDeleteBoardStep())
        .build();
  }

  @Bean
  @JobScope
  public Step backupBoardStep() {
    return stepBuilderFactory.get("hardDeleteBoardJob")
        .<Board, DeletedBoard>chunk(CHUNK_SIZE)
        .reader(backupBoardReader())
        .processor(backupBoardProcessor())
        .writer(backupBoardWriter())
        .build();
  }

  @Bean
  @StepScope
  public RepositoryItemReader<Board> backupBoardReader() {
    return new RepositoryItemReaderBuilder<Board>()
        .name("hardDeleteBoardReader")
        .repository(boardRepository)
        .methodName("findAllByCreatedAtBefore")
        .arguments(hardDeleteBoardJobParam.getCreatedAt())
        .pageSize(CHUNK_SIZE)
        .sorts(Map.of("id", Sort.Direction.ASC))
        .build();
  }

  public ItemProcessor<Board, DeletedBoard> backupBoardProcessor() {
    LocalDateTime now = LocalDateTime.now();
    return board -> DeletedBoard.builder()
        .id(board.getId())
        .title(board.getTitle())
        .content(board.getContent())
        .isDeleted(board.isDeleted())
        .createdAt(board.getCreatedAt())
        .deletedAt(now)
        .build();
  }

  public ItemWriter<DeletedBoard> backupBoardWriter() {
    return new ItemWriter<DeletedBoard>() {
      private StepExecution stepExecution;

      @Override
      public void write(List<? extends DeletedBoard> targetBoards) {
        List<Long> deletedBoardIds = deletedBoardRepository.saveAll(targetBoards)
            .stream()
            .map(DeletedBoard::getId)
            .collect(Collectors.toList());
        ExecutionContext executionContext = this.stepExecution.getJobExecution().getExecutionContext();
        executionContext.put("deletedBoardIds", deletedBoardIds);
      }

      @BeforeStep
      public void setStepExecution(final StepExecution stepExecution) {
        this.stepExecution = stepExecution;
      }
    };
  }

  @Bean
  @JobScope
  public Step hardDeleteBoardStep() {
    return stepBuilderFactory.get("hardDeleteBoardStep")
        .tasklet((contribution, chunkContext) -> {
          Map<String, Object> jobExecutionContext = chunkContext.getStepContext().getJobExecutionContext();
          List<Long> deletedBoardIds = (List<Long>) jobExecutionContext.get("deletedBoardIds");
          boardRepository.deleteAllByIdIn(deletedBoardIds);
          return RepeatStatus.FINISHED;
        })
        .build();
  }
}
