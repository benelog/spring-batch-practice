package kr.co.wikibook.healthchecker.report;

import java.time.LocalDate;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.CallableTaskletAdapter;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SendReportJobConfig {
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager = new ResourcelessTransactionManager();

  public SendReportJobConfig(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  @Bean
  public Job sendReportJob() {
    return new JobBuilder("sendReportJob", jobRepository)
        .start(checkHolidayStep())
        .on("FAILED") // <1>
        .to(buildStep("보고서 전송 없이 종료"))

        .from(checkHolidayStep()) // <2>
        .on("*") // <3>
        .to(buildStep("보고서 전송"))
        .end()
        .build();
  }

  @Bean
  public Step checkHolidayStep() {
    return new StepBuilder("checkHolidayStep", jobRepository)
        .tasklet(checkHolidayTasklet(null), transactionManager)
        .build();
  }

  @Bean
  @JobScope
  public Tasklet checkHolidayTasklet(
      @Value("#{jobParameters['reportDate']}") LocalDate reportDate) {
    var task = new HolidayCheckTask(reportDate);
    return new CallableTaskletAdapter(task);
  }

  private Step buildStep(String stepName) {
    return new StepBuilder(stepName, jobRepository)
        .tasklet(new LoggingTasklet(stepName + " 수행"), transactionManager)
        .build();
  }
}
