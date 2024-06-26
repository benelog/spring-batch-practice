package kr.co.wikibook.batch.healthchecker.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;


class EmailJobReporterTest {
  @Test
  void skipSendEmail() {
    // given
    var jobInstance = new JobInstance(0L, "testJob");
    var jobExecution = new JobExecution(0L);
    jobExecution.setJobInstance(jobInstance);
    jobExecution.setStatus(BatchStatus.COMPLETED);

    boolean skipOnSuccess = true;
    JavaMailSender mailSender = mock(JavaMailSender.class);
    var reporter = new EmailJobReporter(mailSender, List.of(), skipOnSuccess);

    // when
    reporter.afterJob(jobExecution);

    // then
    verify(mailSender, never()).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendEmail() throws IOException {
    try (SimpleSmtpServer smtpServer = SimpleSmtpServer.start(SimpleSmtpServer.AUTO_SMTP_PORT)) {
      // given
      var mailSender = new JavaMailSenderImpl();
      mailSender.setHost("localhost");
      mailSender.setPort(smtpServer.getPort());

      var jobInstance = new JobInstance(0L, "testJob");

      var jobExecution = new JobExecution(0L);
      jobExecution.setJobInstance(jobInstance);
      LocalDateTime startTime = LocalDateTime.parse("2024-03-02T16:02:00");
      LocalDateTime endTime = LocalDateTime.parse("2024-03-02T18:08:45");
      jobExecution.setStatus(BatchStatus.COMPLETED);
      jobExecution.setStartTime(startTime);
      jobExecution.setEndTime(endTime);

      var stepExecution = new StepExecution("testStep", jobExecution, 0L);
      stepExecution.setStartTime(startTime);
      stepExecution.setEndTime(endTime);
      jobExecution.addStepExecutions(List.of(stepExecution));

      var reporter = new EmailJobReporter(
          mailSender,
          List.of("benelog@naver.com"),
          false
      );

      // when
      reporter.afterJob(jobExecution);

      // then
      List<SmtpMessage> emails = smtpServer.getReceivedEmails();
      assertThat(emails).hasSize(1);
      SmtpMessage email = emails.get(0);
      assertThat(email.getHeaderValue("Subject")).isEqualTo("testJob : COMPLETED (2:06:45)");
      assertThat(email.getHeaderValue("To")).isEqualTo("benelog@naver.com");
    }
  }
}
