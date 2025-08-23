package kr.co.wikibook.batch.report.job;


import kr.co.wikibook.batch.report.tasklet.LoggingTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class DeleteReportJobConfig {
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager =
      new ResourcelessTransactionManager();

  public DeleteReportJobConfig(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  @Bean
  public Job deleteReportJob() {
    return new JobBuilder("deleteReportJob", jobRepository)
        .start(buildStep("일간 보고서 삭제"))
        .next(buildStep("주간 보고서 삭제"))
        .on("*")
        .stopAndRestart(buildStep("월간 보고서 삭제"))
        .end()
        .build();
  }

  private Step buildStep(String stepName) {
    return new StepBuilder(stepName, jobRepository)
        .tasklet(new LoggingTasklet(stepName + " 수행"), transactionManager)
        .build();
  }
}
