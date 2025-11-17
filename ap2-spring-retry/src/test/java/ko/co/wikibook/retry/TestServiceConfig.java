package ko.co.wikibook.retry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.resilience.annotation.EnableResilientMethods;

@Configuration
@EnableResilientMethods(proxyTargetClass = true)
public class TestServiceConfig {
	@Bean
	public UnstableNotificationService fail3Service() {
		return new UnstableNotificationService(3);
	}
}
