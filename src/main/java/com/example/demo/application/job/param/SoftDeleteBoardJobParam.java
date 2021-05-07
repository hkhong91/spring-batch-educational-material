package com.example.demo.application.job.param;

import lombok.Getter;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@JobScope
@Getter
public class SoftDeleteBoardJobParam {

  private String createdDate;
  private LocalDateTime createdAt;

  @Value("#{jobParameters[createdDate]}")
  private void setCreatedDate(String createdDate) {
    this.createdAt = LocalDateTime.of(LocalDate.parse(createdDate), LocalTime.MAX);
    this.createdDate = createdDate;
  }
}
