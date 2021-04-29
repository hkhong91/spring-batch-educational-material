package com.example.demo.application.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequiredArgsConstructor
public class JobController {

  private final Job createArticlesJob;
  private final Job createArticlesPartitioningJob;
  private final JobLauncher jobLauncher;

  @GetMapping("/jobs/create-articles")
  public void createArticles(@RequestParam String createdDate) throws Exception {
    this.jobLauncher.run(this.createArticlesJob, new JobParametersBuilder()
        .addDate("date", new Date())
        .addString("createdDate", createdDate)
        .toJobParameters());
  }

  @GetMapping("/jobs/create-articles-partitioning")
  public void createArticlesPartitioning(@RequestParam String createdDate) throws Exception {
    this.jobLauncher.run(this.createArticlesPartitioningJob, new JobParametersBuilder()
        .addDate("date", new Date())
        .addString("createdDate", createdDate)
        .toJobParameters());
  }
}
