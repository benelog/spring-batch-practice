package ko.co.wikibook.retry;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(TestServiceConfig.class)
class NotificationCircuitServiceTest {

	@Autowired
	FragileService fail2Service;

	private void callAndAssert(boolean expectedResult, int expectedTryCount) {
		boolean success = fail2Service.sendOnCircuit("Hello!");
		assertThat(success).isEqualTo(expectedResult);
		assertThat(fail2Service.getTryCount()).isEqualTo(expectedTryCount);
	}

	@Test
	void recoveredWithCircuit() throws InterruptedException {
		callAndAssert(false, 1);
		callAndAssert( false, 2);
		callAndAssert( false, 2);
		TimeUnit.MILLISECONDS.sleep(310);
		callAndAssert(true, 3);
	}
}
