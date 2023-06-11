package kr.co.wikibook.hello.tasklet;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class HelloLocalDateTasklet implements Tasklet {

  private final Logger log = LoggerFactory.getLogger(HelloLocalDateTasklet.class);
  private final LocalDate date;

  public HelloLocalDateTasklet(LocalDate date) {
    this.date = date;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    log.info("Hello {}", date);
    return RepeatStatus.FINISHED;
  }
}
