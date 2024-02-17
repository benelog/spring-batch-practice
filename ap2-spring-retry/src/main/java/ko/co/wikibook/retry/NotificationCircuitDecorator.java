package ko.co.wikibook.retry;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.classify.BinaryExceptionClassifier;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryOperations;
import org.springframework.retry.RetryState;
import org.springframework.retry.policy.CircuitBreakerRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.DefaultRetryState;
import org.springframework.retry.support.RetryTemplate;

public class NotificationCircuitDecorator implements NotificationService {
	private final Logger logger = LoggerFactory.getLogger(NotificationCircuitDecorator.class);
	private final NotificationService target;
	private final RetryOperations retryOperations;
	private final RetryState state;


	public NotificationCircuitDecorator(NotificationService target, int maxAttempts) {
		this.target = target;
		this.retryOperations = buildRetryOperations(maxAttempts);
		String stateKey = "NotificationService.send";
		var rollbackClassifier = new BinaryExceptionClassifier(false);
		this.state = new DefaultRetryState(stateKey, rollbackClassifier);
	}

	@Override
	public boolean send(String message) {
		RetryCallback<Boolean, RuntimeException> retryCallback = (RetryContext context) -> { // <4>
			logger.info("{}", context);
			return this.target.send(message);
		};
		return this.retryOperations.execute(
			retryCallback,
			(RetryContext context) -> recover(context.getLastThrowable(), message),
			this.state
		);
	}

	private boolean recover(Throwable error, String message) {
		logger.warn("메시지 전송 최종 실패 후 recover : {}", message, error);
		return false;
	}

	private RetryOperations buildRetryOperations(int maxAttempts) {
		var simplePolicy = new SimpleRetryPolicy(
			maxAttempts,
			Map.of(RuntimeException.class, true)
		);
		var circuitBreakerPolicy = new CircuitBreakerRetryPolicy(simplePolicy);
		circuitBreakerPolicy.setOpenTimeout(200L);
		circuitBreakerPolicy.setResetTimeout(300L);

		var retryTemplate = new RetryTemplate();
		retryTemplate.setRetryPolicy(circuitBreakerPolicy);
		return retryTemplate;
	}
}
