package ko.co.wikibook.retry;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class NotificationCircuitDecoratorTest {
	UnstableNotificationService target = new UnstableNotificationService(2);
	NotificationCircuitDecorator decorator = new NotificationCircuitDecorator(target, 2);

	private void callAndAssert(boolean expectedResult, int expectedTryCount) {
		boolean success = decorator.send("Hello!");
		assertThat(success).isEqualTo(expectedResult);
		assertThat(target.getTryCount()).isEqualTo(expectedTryCount);
	}

	@Test
	void recoveredWithCircuit() throws InterruptedException {
		callAndAssert(false, 1);
		callAndAssert(false, 2);
		callAndAssert(false, 2);
		TimeUnit.MILLISECONDS.sleep(310);
		callAndAssert(true, 3);
	}
}
