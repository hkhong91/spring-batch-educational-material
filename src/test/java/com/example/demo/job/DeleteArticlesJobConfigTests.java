package com.example.demo.job;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DeleteArticlesJobConfigTests {

  @Autowired
  private Job deleteArticlesJob;

  @Autowired
  private JobLauncher jobLauncher;

  /**
   * java -jar ./build/libs/demo-batch-1.0.0.jar --job.name=deleteArticlesJob -createdDate=2020-09-18
   *
   * @throws Exception exception
   */
  @Test
  public void run() throws Exception {
    this.jobLauncher.run(this.deleteArticlesJob, new JobParametersBuilder()
        .addString("createdDate", "2020-09-18")
        .toJobParameters());
  }
}
