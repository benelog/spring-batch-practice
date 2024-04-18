package ko.co.wikibook.retry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NotificationRetryDecoratorTest {
	@Test
	void successByRetry() {
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
	void recover() {
		// given
		var target = new UnstableNotificationService(2);
		var decorator = new NotificationRetryDecorator(target, 2);

		// when
		boolean success = decorator.send("hello");

		// then
		assertThat(success).isFalse();
		assertThat(target.getTryCount()).isEqualTo(2);
	}

	@Test
	void successOnFirstTry() {
		NotificationService target = (message) -> {
			System.out.println("Message : " + message);
			return true;
		};
		var decorator = new NotificationRetryDecorator(target, 2);

		boolean success = decorator.send("hello");
		assertThat(success).isTrue();
	}
}
