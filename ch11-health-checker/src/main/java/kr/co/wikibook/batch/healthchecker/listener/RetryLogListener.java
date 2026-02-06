package kr.co.wikibook.batch.healthchecker.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

public class RetryLogListener implements RetryListener {

  private final Logger logger = LoggerFactory.getLogger(RetryLogListener.class);

  @Override
  public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
    logger.info("onOpen: {}", context);
    return true;
  }

  @Override
  public <T, E extends Throwable> void close(
      RetryContext context, RetryCallback<T, E> callback,
      Throwable throwable) {
    logger.info("onClose: {}", context);
  }

  @Override
  public <T, E extends Throwable> void onSuccess(
      RetryContext context, RetryCallback<T, E> callback,
      T result
  ) {
    logger.info("onSuccess: {}", context);
  }

  @Override
  public <T, E extends Throwable> void onError(
      RetryContext context, RetryCallback<T, E> callback,
      Throwable throwable) {
    logger.info("onError: {}", context);
  }
}
