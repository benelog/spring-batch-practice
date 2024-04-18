package ko.co.wikibook.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

public class RetryLoggingListener implements RetryListener {
	private final Logger logger = LoggerFactory.getLogger(RetryLoggingListener.class);

	@Override
	public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
		logger.info("open : {}", context);
		return true;
	}

	@Override
	public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
		Throwable throwable) {
		logger.warn("onError: {}", context, throwable);
	}

	@Override
	public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback,
		Throwable throwable) {
		logger.info("close : {}", context);
	}

	@Override
	public <T, E extends Throwable> void onSuccess(RetryContext context, RetryCallback<T, E> callback, T result) {
		logger.info("onSuccess : {}", result);
	}
}
