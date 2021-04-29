package com.example.demo.application.job.param;

import java.time.LocalDate;
import lombok.Getter;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@JobScope
@Getter
public class CreateArticlesPartitioningJobParam {

  private LocalDate createdDate;

  @Value("#{jobParameters[createdDate]}")
  public void setCreatedDate(String createdDate) {
    this.createdDate = LocalDate.parse(createdDate);
  }
}
