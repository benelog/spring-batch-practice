package ko.co.wikibook.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogginngService implements NotificationService{
	private final Logger logger = LoggerFactory.getLogger(LogginngService.class);

	@Override
	public boolean send(String message) {
		logger.info(message);
		return true;
	}
}
