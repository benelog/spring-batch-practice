package ko.co.wikibook.retry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NotificationRetryDecoratorTest {
	@Test
	void retry() {
		// given
		var target = new UnstableNotificationService(3);
		var decorator = new NotificationRetryDecorator(target, 4);

		// when
		boolean success = decorator.send("hello");

		// then
		assertThat(success).isTrue();
		assertThat(target.getTryCount()).isEqualTo(4);
	}

	@Test
	void retryWithRecover() {
		// given
		var target = new UnstableNotificationService(2);
		var decorator = new NotificationRetryDecorator(target, 2);

		// when
		boolean success = decorator.send("hello");

		// then
		assertThat(success).isFalse();
		assertThat(target.getTryCount()).isEqualTo(2);
	}
}
