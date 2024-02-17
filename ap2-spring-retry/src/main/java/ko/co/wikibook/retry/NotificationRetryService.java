package ko.co.wikibook.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

interface NotificationRetryService extends NotificationService {
	Logger logger = LoggerFactory.getLogger(NotificationRetryService.class);

	@Retryable(
		retryFor = RuntimeException.class, maxAttempts = 4,
		backoff = @Backoff(delay = 200L, multiplier = 2d, maxDelay = 600L)
	)
	boolean send(String message);

	@Recover
	default boolean recover(Throwable error, String message) {
		logger.warn("메시지 전송 최종 실패 후 recover : {}", message, error);
		return false;
	}
}
