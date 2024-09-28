package kr.co.wikibook.batch.healthchecker.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.*;

import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;

class ReportFormatDecideTaskletTest {
  @ParameterizedTest
  @MethodSource("provideDateAndReportFormat")
  void execute(LocalDate reportDate, ReportFormat format) {
    // given
    var jobParameter = new JobParametersBuilder()
        .addLocalDate("reportDate", reportDate)
        .toJobParameters();
    var stepExecution = new StepExecution("formatDecideStep", new JobExecution(1L, jobParameter));
    var stepContribution = new StepContribution(stepExecution);
    var chunkContext = new ChunkContext(new StepContext(stepExecution));
    ReportFormatDecideTasklet tasklet = new ReportFormatDecideTasklet();

    // when
    tasklet.execute(stepContribution, chunkContext);
    ExitStatus exitStatus = tasklet.afterStep(stepExecution);

    // then
    assertThat(exitStatus.getExitCode()).isEqualTo(format.name());
  }

  static Stream<Arguments> provideDateAndReportFormat() {
    return Stream.of(
        Arguments.of(LocalDate.of(2024, 3, 13), ReportFormat.DAILY),
        Arguments.of(LocalDate.of(2024, 3, 18), ReportFormat.WEEKLY),
        Arguments.of(LocalDate.of(2024, 4, 1), ReportFormat.MONTHLY)
    );
  }
}