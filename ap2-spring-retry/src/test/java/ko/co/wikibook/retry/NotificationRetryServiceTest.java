package ko.co.wikibook.retry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(TestServiceConfig.class)
class NotificationRetryServiceTest {
	@Test
	void successByRetry(@Qualifier("fail3Service") UnstableNotificationService fail3Service) {
		boolean success = fail3Service.send("Hello!");
		assertThat(fail3Service.getTryCount()).isEqualTo(4);
		assertThat(success).isTrue();
	}

	@Test
	void recover(@Qualifier("fail4Service") UnstableNotificationService fail4Service) {
		boolean success = fail4Service.send("Hello!");
		assertThat(fail4Service.getTryCount()).isEqualTo(4);
		assertThat(success).isFalse();
	}
}
