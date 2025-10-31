
package com.example.ms_batch_operaciones.web;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs")
public class JobController {
  private final JobLauncher jobLauncher;
  private final Job csvToKafkaJob;

  public JobController(JobLauncher jobLauncher, Job csvToKafkaJob) {
    this.jobLauncher = jobLauncher;
    this.csvToKafkaJob = csvToKafkaJob;
  }

  @PostMapping("/csv-to-kafka/run")
  public ResponseEntity<String> run() throws Exception {
    JobParameters params = new JobParametersBuilder()
        .addLong("time", System.currentTimeMillis())
        .toJobParameters();
    jobLauncher.run(csvToKafkaJob, params);
    return ResponseEntity.ok("csvToKafkaJob launched");
  }
}
