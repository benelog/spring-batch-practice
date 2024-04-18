package ko.co.wikibook.retry;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryOperations;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

public class NotificationRetryDecorator implements NotificationService {
	private final Logger logger = LoggerFactory.getLogger(NotificationRetryDecorator.class);
	private final NotificationService target;
	private final RetryOperations retryOperations;

	public NotificationRetryDecorator(NotificationService target, int maxAttempts) {
		this.target = target;
		this.retryOperations = RetryTemplate.builder()
			.retryOn(RuntimeException.class)
			.maxAttempts(maxAttempts)
			.exponentialBackoff(200L, 2d, 600L)
			.withListener(new RetryLoggingListener())
			.build();
	}

	@Override
	public boolean send(String message) {
		return this.retryOperations.execute(
			(context) -> this.target.send(message),
			(context) -> recover(context.getLastThrowable(), message)
		);
	}

	private boolean recover(Throwable error, String message) {
		logger.warn("메시지 전송 최종 실패 후 recover : {}", message, error);
		return false;
	}

	private RetryOperations buildRetryOperations(int maxAttempts) {
		var retryPolicy = new SimpleRetryPolicy(
			maxAttempts,
			Map.of(RuntimeException.class, true)
		);
		var backOffPolicy = new ExponentialBackOffPolicy(); // <5>
		backOffPolicy.setInitialInterval(200L);
		backOffPolicy.setMultiplier(2d);
		backOffPolicy.setMaxInterval(600L);

		var retryTemplate = new RetryTemplate(); // <6>
		retryTemplate.setRetryPolicy(retryPolicy);
		retryTemplate.setBackOffPolicy(backOffPolicy);
		return retryTemplate;
	}
}
