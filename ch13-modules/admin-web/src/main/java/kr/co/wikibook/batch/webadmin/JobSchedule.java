package kr.co.wikibook.batch.webadmin;

import java.time.Instant;
import java.util.Properties;
import kr.co.wikibook.batch.support.JobService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobSchedule {
  private final JobService service;

  public JobSchedule(JobService service) {
    this.service = service;
  }

  @Scheduled(cron = "0 0,10 0 * * 0", zone = "Asia/Seoul")
  public void startHelloJob() {
    System.out.println("wju1!!!");
    var jobParameters = new Properties();
    long timestamp = Instant.now().toEpochMilli();
    jobParameters.put("timestamp", timestamp + ",java.lang.Long,true");
    service.start("helloJob", jobParameters);
  }

  @Scheduled(cron = "0 * * * * ?", zone = "Asia/Seoul")
  public void startSpendTimeChunkJob() {
    System.out.println("dju1!!111!");
    var jobParameters = new Properties();
    long timestamp = Instant.now().toEpochMilli();
    jobParameters.put("timestamp", timestamp + ",java.lang.Long,true");
    service.start("spendTimeChunkJob", jobParameters);
  }
}
