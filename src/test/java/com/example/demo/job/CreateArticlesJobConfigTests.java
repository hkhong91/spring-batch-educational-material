package com.example.demo.job;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
public class CreateArticlesJobConfigTests {

  @Autowired
  private Job createArticlesJob;

  @Autowired
  private JobLauncher jobLauncher;

  @Test
  public void run() throws Exception {
    this.jobLauncher.run(this.createArticlesJob, new JobParametersBuilder()
        .addDate("date", new Date())
        .addString("fileName", "Articles.csv")
        .toJobParameters());
  }
}
