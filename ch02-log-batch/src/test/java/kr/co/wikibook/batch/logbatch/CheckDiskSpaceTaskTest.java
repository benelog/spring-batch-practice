package kr.co.wikibook.batch.logbatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CheckDiskSpaceTaskTest {

  MockNotificationService notificationService = new MockNotificationService();
  CheckDiskSpaceTask task = new CheckDiskSpaceTask(notificationService);

  @DisplayName("지정된 디렉토리가 없으면 아무것도 하지 않는다.")
  @Test
  void doNothingWhenEmptyArgument() {
    task.run();
  }

  @DisplayName("디스크 용량이 기대치보다 많다")
  @Test
  void checkDiskSpaceWhenSufficient() {
    task.run("/", "1");
    String message = notificationService.getLastMessage();
    assertThat(message.matches("남은 용량 \\d{1,3}%"));
  }

  @DisplayName("디스크 용량이 기대치보다 적다")
  @Test
  void checkDiskSpaceWhenInsufficient() {
    assertThatThrownBy(() ->
        task.run("/", "100")
    ).isInstanceOf(IllegalStateException.class);
  }
}
