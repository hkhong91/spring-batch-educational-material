package com.example.demo.application.job.param;

import lombok.Getter;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@JobScope
@Getter
public class CreateArticlesJobParam {

  private String fileName;

  @Value("#{jobParameters[fileName]}")
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
}
