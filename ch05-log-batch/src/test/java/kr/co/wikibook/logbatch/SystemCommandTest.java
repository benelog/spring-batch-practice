package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.SystemCommandException;
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.core.io.ClassPathResource;

class SystemCommandTest {
  SystemCommandTasklet systemCommandTasklet = new SystemCommandTasklet();
  StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
  StepContribution stepContribution = new StepContribution(stepExecution);
  ChunkContext chunkContext = new ChunkContext(new StepContext(stepExecution));

  @Test
  @EnabledOnOs({OS.LINUX, OS.MAC}) // <1>
  void execute(@TempDir Path tempPath) throws Exception {
    // given
    String shellPath = readAbsolutePath("command.sh"); // <2>
    systemCommandTasklet.setCommand(shellPath); // <3>
    systemCommandTasklet.setTimeout(1000); // <4>
    systemCommandTasklet.setEnvironmentParams(new String[]{"MESSAGE=Hello"}); // <5>
    systemCommandTasklet.setWorkingDirectory(tempPath.toString()); // <6>
    systemCommandTasklet.setCommandRunner(new ConsoleOutputCommandRunner());

    systemCommandTasklet.afterPropertiesSet(); // <7>

    // when
    RepeatStatus taskStatus = systemCommandTasklet.execute(stepContribution, chunkContext);

    // then
    assertThat(stepContribution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    assertThat(taskStatus).isEqualTo(RepeatStatus.FINISHED);
    String content = Files.readString(tempPath.resolve("output.txt"));
    assertThat(content).isEqualTo("Hello\n"); // <8>
  }

  @Test
  @EnabledOnOs({OS.LINUX, OS.MAC}) // <1>
  void executeTimeout() throws Exception {
    systemCommandTasklet.setCommand("sleep", "3000");
    systemCommandTasklet.setTimeout(100);
    systemCommandTasklet.afterPropertiesSet();

    assertThatExceptionOfType(SystemCommandException.class)
        .isThrownBy(() -> systemCommandTasklet.execute(stepContribution, chunkContext))
        .withMessageContaining("Execution of system command did not finish within the timeout");
  }

  @Test
  void executeEcho() throws Exception {
    systemCommandTasklet.setCommand("echo", "hello");
    systemCommandTasklet.setTimeout(1000);
    systemCommandTasklet.setCommandRunner(new ConsoleOutputCommandRunner());
    systemCommandTasklet.afterPropertiesSet();
    systemCommandTasklet.execute(stepContribution, chunkContext);
  }

  private String readAbsolutePath(String classPathFile) throws IOException {
    ClassPathResource resource = new ClassPathResource(classPathFile);
    return resource.getFile().getAbsolutePath();
  }
}
