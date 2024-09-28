package kr.co.wikibook.batch.hello.tasklet;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class HelloDate2Tasklet implements Tasklet {
  private final Logger log = LoggerFactory.getLogger(HelloDate2Tasklet.class);

  private final LocalDate helloDate;

  public HelloDate2Tasklet(LocalDate helloDate) {
    this.helloDate = helloDate;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    log.info("Hello {} ", this.helloDate);
    return RepeatStatus.FINISHED;
  }
}
