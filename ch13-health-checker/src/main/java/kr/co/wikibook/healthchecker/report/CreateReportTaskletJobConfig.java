package kr.co.wikibook.healthchecker.report;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 13장에서 ReportFormatDecider로 대체하기 전, ReportFormatDecideTasklet으로 분기를 판단하던 구성이다.
 * 최종 구성인 CreateReportJobConfig와 잡 이름이 겹치지 않도록 'createReportTaskletJob'으로 등록한다.
 */
@Configuration
public class CreateReportTaskletJobConfig {
  private final JobRepository jobRepository;

  public CreateReportTaskletJobConfig(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  @Bean
  public Job createReportTaskletJob() {
    return new JobBuilder("createReportTaskletJob", jobRepository)
        .start(reportFormatDecideStep())

        .on(ReportFormat.DAILY.name()) // <1>
        .to(buildStep("일간 보고서 생성(태스클릿 분기)"))

        .from(reportFormatDecideStep()) // <2>
        .on(ReportFormat.WEEKLY.name())
        .to(buildStep("주간 보고서 생성(태스클릿 분기)"))

        .from(reportFormatDecideStep()) // <3>
        .on(ReportFormat.MONTHLY.name())
        .to(buildStep("월간 보고서 생성(태스클릿 분기)"))

        .from(reportFormatDecideStep())
        .on("*")
        .fail() // <4>
        .end()
        .build();
  }

  @Bean // <5>
  public Step reportFormatDecideStep() {
    return new StepBuilder("reportFormatDecideStep", jobRepository)
        .tasklet(new ReportFormatDecideTasklet())
        .build();
  }

  private Step buildStep(String stepName) {
    return new StepBuilder(stepName, jobRepository)
        .tasklet(new LoggingTasklet(stepName + " 수행"))
        .build();
  }
}
