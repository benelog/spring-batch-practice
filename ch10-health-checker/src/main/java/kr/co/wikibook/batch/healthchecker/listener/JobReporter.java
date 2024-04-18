package kr.co.wikibook.batch.healthchecker.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;

public class JobReporter implements JobExecutionListener {
  private final Logger logger = LoggerFactory.getLogger(JobReporter.class);

  @Override
  public void afterJob(JobExecution jobExec) {
    String jobName = jobExec.getJobInstance().getJobName();
    logger.info("Job summary report: [{}]", jobName);
    for (StepExecution stepExec : jobExec.getStepExecutions()) { // <3>
      logger.info("[{}]: {}", stepExec.getStepName(), stepExec.getSummary());
    }
  }
}
