package com.example.demo.job;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UpdateArticlesJobConfigTests {

  @Autowired
  private Job updateArticlesJob;

  @Autowired
  private JobLauncher jobLauncher;

  @Test
  public void run() throws Exception {
    this.jobLauncher.run(this.updateArticlesJob, new JobParametersBuilder()
        .addString("createdDate", "2020-08-21")
        .toJobParameters());
  }
}
