package kr.co.wikibook.batch.tasklet;

import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class HelloDateTasklet implements Tasklet {

  private final Logger log = LoggerFactory.getLogger(HelloDateTasklet.class);

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
    Date helloDay = (Date) jobParameters.get("helloDate");
    log.info("Hello {}", helloDay);
    return RepeatStatus.FINISHED;
  }
}
