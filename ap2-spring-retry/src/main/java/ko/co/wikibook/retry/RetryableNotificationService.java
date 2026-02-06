package ko.co.wikibook.retry;

import org.springframework.resilience.annotation.Retryable;

interface RetryableNotificationService extends NotificationService{
	@Retryable(
			includes = RuntimeException.class,
			maxRetries = 4,
			delay = 200L, multiplier = 2d, maxDelay = 600L
	)
	void send(String message);

	int getTryCount();
}
