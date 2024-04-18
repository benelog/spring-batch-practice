package kr.co.wikibook.batch.healthchecker.report;

import java.time.DayOfWeek;
import java.time.LocalDate;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

public class ReportFormatDecider implements JobExecutionDecider {
  @Override
  public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
    JobParameters jobParameters = jobExecution.getJobParameters();
    LocalDate reportDate = jobParameters.getLocalDate("reportDate");
    ReportFormat reportFormat = getReportFormat(reportDate);
    return new FlowExecutionStatus(reportFormat.name());
  }

  private ReportFormat getReportFormat(LocalDate reportDate) {
    if (reportDate.getDayOfMonth() == 1) {
      return ReportFormat.MONTHLY;
    }

    if (reportDate.getDayOfWeek() == DayOfWeek.MONDAY) {
      return ReportFormat.WEEKLY;
    }

    return ReportFormat.DAILY;
  }
}
