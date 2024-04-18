package kr.co.wikibook.batch.healthchecker.listener;

import java.nio.file.Path;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

public class RetryItemRecorder extends FileRecorder implements RetryListener {
  public RetryItemRecorder(Path recordPath) {
    super(recordPath);
  }

  public <T, E extends Throwable> void onSuccess(
      RetryContext context, RetryCallback<T, E> callback,
      T result
  ) {
    if (result == null) {
      return;
    }

    if (context.getRetryCount() > 1) {
      writeLine(result.toString());
    }
  }
}
