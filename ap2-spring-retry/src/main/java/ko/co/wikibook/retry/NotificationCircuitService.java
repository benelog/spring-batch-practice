package ko.co.wikibook.retry;

import org.springframework.retry.annotation.CircuitBreaker;

interface NotificationCircuitService {

	@CircuitBreaker(
		retryFor = RuntimeException.class,
		maxAttempts = 2, openTimeout = 200L, resetTimeout = 300L
	)
	boolean sendOnCircuit(String message);
}
