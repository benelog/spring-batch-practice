package kr.co.wikibook.batch.webadmin;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;

class JobScheduleTest {
  @Test
  void scheduleHelloJob() {
    var initialTime = LocalDateTime.of(2024, 6, 10, 0, 5);
    List<LocalDateTime> expectedTimes = List.of(
        LocalDateTime.of(2024, 6, 16, 0, 0),
        LocalDateTime.of(2024, 6, 16, 0, 10),
        LocalDateTime.of(2024, 6, 23, 0, 0)
    );
    ScheduleTestUtils.assertCronExpression(
        JobSchedule.class, "startHelloJob",
        toInstant(initialTime),
        expectedTimes.stream().map(this::toInstant).toList()
    );
  }

  private Instant toInstant(LocalDateTime time) {
    return time.atZone(ZoneId.of("Asia/Seoul")).toInstant();
  }
}
