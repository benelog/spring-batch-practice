package kr.co.wikibook.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogginngService implements NotificationService{
	private final Logger logger = LoggerFactory.getLogger(LogginngService.class);

	@Override
	public void send(String message) {
		logger.info(message);
	}
}
