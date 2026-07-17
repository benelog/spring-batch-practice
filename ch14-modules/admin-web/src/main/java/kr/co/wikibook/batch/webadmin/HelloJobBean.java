package kr.co.wikibook.batch.webadmin;

import java.time.Instant;
import java.util.Properties;
import kr.co.wikibook.batch.support.JobService;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class HelloJobBean extends QuartzJobBean {
  private final JobService service;

  public HelloJobBean(JobService service) {
    this.service = service;
  }

  @Override
  protected void executeInternal(JobExecutionContext context) {
    var jobParameters = new Properties();
    long timestamp = Instant.now().toEpochMilli();
    jobParameters.put("timestamp", timestamp + ",java.lang.Long,true");
    service.start("helloJob", jobParameters);
  }
}
