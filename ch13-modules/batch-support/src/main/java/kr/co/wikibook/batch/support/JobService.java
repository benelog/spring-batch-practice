package kr.co.wikibook.batch.support;

import java.util.Properties;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.converter.JobParametersConverter;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;

public class JobService {
  private final JobLocator locator;
  private final JobLauncher launcher;
  private final JobExplorer explorer;
  private JobParametersConverter converter = new DefaultJobParametersConverter();

  public JobService(JobLocator locator, JobLauncher jobLauncher, JobExplorer jobExplorer) {
    this.locator = locator;
    this.launcher = jobLauncher;
    this.explorer = jobExplorer;
  }

  public long start(String jobName, Properties parameters) {
    try {
      Job job = locator.getJob(jobName);
      JobParameters jobParameters = converter.getJobParameters(parameters);
      if (job.getJobParametersIncrementer() != null) {
        jobParameters = new JobParametersBuilder(jobParameters, explorer)
            .getNextJobParameters(job)
            .toJobParameters();
      }
      JobExecution execution = launcher.run(job, jobParameters);
      return execution.getId();
    } catch (JobExecutionException ex) {
      throw new IllegalArgumentException(ex);
    }
  }
}
