package kr.co.wikibook.healthchecker.report;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.job.DefaultJobParametersExtractor;
import org.springframework.batch.core.step.job.JobParametersExtractor;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CreateAndSendReportJobConfig {
  @Bean
  public Job createAndSendReportJob(
      JobRepository jobRepository,
      @Qualifier("createReportJob") Job createReportJob,
      @Qualifier("sendReportJob") Job sendReportJob
  ) {

    var launcher = new TaskExecutorJobLauncher();
    launcher.setJobRepository(jobRepository);
    launcher.setTaskExecutor(new SimpleAsyncTaskExecutor());

    Step createReportStep = new StepBuilder("createReportStep", jobRepository)
        .job(createReportJob)
        .launcher(launcher)
        .build();

    var extractor = new DefaultJobParametersExtractor();
    extractor.setUseAllParentParameters(false);
    extractor.setKeys(new String[] {"reportDate", "runId"});

    Step sendReportStep = new StepBuilder("sendReportStep", jobRepository)
        .job(sendReportJob)
        .parametersExtractor(extractor)
        .build();

    return new JobBuilder("createAndSendReportJob", jobRepository)
        .start(createReportStep)
        .next(sendReportStep)
        .build();
  }
}
