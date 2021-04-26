package com.example.demo.application.job.param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

@Getter
public class DeleteArticlesJobParam {

  private String createdDate;
  private LocalDateTime createdAt;

  @Value("#{jobParameters[createdDate]}")
  public void setCreatedDate(String createdDate) {
    this.createdDate = createdDate;
    this.createdAt = LocalDateTime.of(LocalDate.parse(createdDate), LocalTime.MIN);
  }

}
