package kr.co.wikibook.batch.webadmin;

import java.util.TimeZone;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzJobSchedule {
  @Bean
  public Trigger helloJobCronTrigger() {
    var schedule = CronScheduleBuilder
        .dailyAtHourAndMinute(13,0)
        .inTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

    return TriggerBuilder.newTrigger()
        .forJob(helloJobDetail())
        .withIdentity("helloJobCronTrigger")
        .startNow()
        .withSchedule(schedule)
        .build();
  }

  @Bean
  public Trigger helloJobSimpleTrigger() {
    var schedule = SimpleScheduleBuilder.simpleSchedule()
        .withIntervalInMinutes(10)
        .repeatForever();

    return TriggerBuilder.newTrigger()
        .forJob(helloJobDetail())
        .withIdentity("helloJobSimpleTrigger")
        .startNow()
        .withSchedule(schedule)
        .build();
  }

  @Bean
  public JobDetail helloJobDetail() {
    return JobBuilder.newJob(HelloJobBean.class)
        .withIdentity("helloJobDetail")
        .storeDurably(true)
        .build();
  }
}
