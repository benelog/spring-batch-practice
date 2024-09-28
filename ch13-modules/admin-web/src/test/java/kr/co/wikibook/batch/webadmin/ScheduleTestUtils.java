package kr.co.wikibook.batch.webadmin;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

public class ScheduleTestUtils {
  public static void assertCronExpression(
      Class<?> targetClass, String methodName,
      Instant initialTime, List<Instant> expectedTimes
  ) {
    Method method = ReflectionUtils.findMethod(targetClass, methodName);
    assertThat(method).isNotNull();

    Scheduled scheduled = method.getAnnotation(Scheduled.class);
    CronTrigger trigger = getTrigger(scheduled);
    var context = new SimpleTriggerContext(initialTime, initialTime, initialTime);

    for (Instant expected : expectedTimes) {
      Instant actual = trigger.nextExecution(context);
      assertThat(actual).isEqualTo(expected);
      context.update(actual, actual, actual);
    }
  }

  private static CronTrigger getTrigger(Scheduled scheduled) {
    String cron = scheduled.cron();
    // 스프링의 ScheduledAnnotationBeanPostProcessor 코드를 참고함
    if (StringUtils.hasText(scheduled.zone())) {
      return new CronTrigger(cron, StringUtils.parseTimeZoneString(scheduled.zone()));
    } else {
      return new CronTrigger(cron);
    }
  }
}