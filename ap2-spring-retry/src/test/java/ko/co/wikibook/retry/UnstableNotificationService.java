package ko.co.wikibook.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.resilience.annotation.Retryable;

public class UnstableNotificationService implements NotificationService {

  private final Logger logger = LoggerFactory.getLogger(UnstableNotificationService.class);
  private final int failures;
  private int tryCount = 0;

  public UnstableNotificationService(int failures) {
    this.failures = failures;
  }

  @Retryable(
      includes = RuntimeException.class,
      maxAttempts = 4,
      delay = 200L, multiplier = 2d, maxDelay = 600L
  )
  @Override
  public void send(String message) {
    this.tryCount++;
    if (this.tryCount <= this.failures) {
      throw new RuntimeException("실패 : " + tryCount);
    }
    logger.info("성공 : {}, {}", this.tryCount, message);
  }

  public int getTryCount() {
    return this.tryCount;
  }
}
