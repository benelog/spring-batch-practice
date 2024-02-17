package kr.co.wikibook.batch.logbatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
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

  SystemCommandTasklet tasklet = new SystemCommandTasklet();
  StepContribution stepContribution = null;
  ChunkContext chunkContext = null;

  @BeforeEach
  void setUp() {
    StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
    stepContribution = new StepContribution(stepExecution);
    chunkContext = new ChunkContext(new StepContext(stepExecution));
  }

  @Test
  @EnabledOnOs(OS.LINUX)
  void execute(@TempDir Path tempPath) throws Exception {
    // given
    String shellPath = readAbsolutePath("command.sh");
    tasklet.setCommand(shellPath);
    tasklet.setTimeout(1000);
    tasklet.setEnvironmentParams(new String[]{"MESSAGE=Hello"});
    tasklet.setWorkingDirectory(tempPath.toString());
    tasklet.afterPropertiesSet();

    // when
    RepeatStatus taskStatus = tasklet.execute(stepContribution, chunkContext);

    // then
    assertThat(stepContribution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
    assertThat(taskStatus).isEqualTo(RepeatStatus.FINISHED);
    String content = Files.readString(tempPath.resolve("output.txt"));
    assertThat(content).isEqualTo("Hello\n"); // <8>
  }

  @Test
  void executeTimeout() throws Exception {
    tasklet.setCommand("sleep", "3000");
    tasklet.setTimeout(100);
    tasklet.afterPropertiesSet();

    assertThatExceptionOfType(SystemCommandException.class)
        .isThrownBy(() -> tasklet.execute(stepContribution, chunkContext))
        .withMessageContaining("Execution of system command did not finish within the timeout");
  }

  @Test
  void executeEcho() throws Exception {
    tasklet.setCommand("echo", "hello");
    tasklet.setTimeout(1000);
    tasklet.setCommandRunner(new ConsoleOutputCommandRunner());
    tasklet.afterPropertiesSet();
    tasklet.execute(stepContribution, chunkContext);
  }

  private String readAbsolutePath(String classPathFile) throws IOException {
    var resource = new ClassPathResource(classPathFile);
    return resource.getFile().getAbsolutePath();
  }
}
