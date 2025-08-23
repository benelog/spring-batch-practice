package kr.co.wikibook.healthchecker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HealthCheckerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HealthCheckerApplication.class, args);
	}

	@Bean
	public String test1() {
		return "";
	}

}
