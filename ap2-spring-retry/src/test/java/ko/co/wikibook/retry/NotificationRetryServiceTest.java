package ko.co.wikibook.retry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(TestServiceConfig.class)
class NotificationRetryServiceTest {
  @Test
  void successByRetry(@Qualifier("fail3Service") UnstableNotificationService fail3Service) {
    fail3Service.send("Hello!");
    assertThat(fail3Service.getTryCount()).isEqualTo(4);
  }
}
