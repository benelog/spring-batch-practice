package kr.co.wikibook.batch.healthchecker.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;

class ReportFormatDeciderTest {
  @ParameterizedTest
  @MethodSource("provideDateAndReportFormat")
  void decide(LocalDate reportDate, ReportFormat format) {
    var jobParameter = new JobParametersBuilder()
        .addLocalDate("reportDate", reportDate)
        .toJobParameters();
    var jobExecution = new JobExecution(1L, jobParameter);
    var stepExecution = new StepExecution("formatDecideStep", jobExecution);

    FlowExecutionStatus executionStatus = new ReportFormatDecider().decide(jobExecution, stepExecution);

    assertThat(executionStatus.getName()).isEqualTo(format.name());
  }

  static Stream<Arguments> provideDateAndReportFormat() {
    return Stream.of(
        Arguments.of(LocalDate.of(2024, 3, 13), ReportFormat.DAILY),
        Arguments.of(LocalDate.of(2024, 3, 18), ReportFormat.WEEKLY),
        Arguments.of(LocalDate.of(2024, 4, 1), ReportFormat.MONTHLY)
    );
  }
}