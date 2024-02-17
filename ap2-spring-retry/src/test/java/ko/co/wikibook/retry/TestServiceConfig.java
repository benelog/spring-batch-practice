package ko.co.wikibook.retry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry(proxyTargetClass = true)
public class TestServiceConfig {
	@Bean
	public UnstableNotificationService fail3Service() {
		return new UnstableNotificationService(3);
	}

	@Bean
	public UnstableNotificationService fail4Service() {
		return new UnstableNotificationService(4);
	}

	@Bean
	public FragileService fail2Service() {
		return new FragileService(2);
	}
}
