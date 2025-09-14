package kr.co.wikibook.hello;

import kr.co.wikibook.hello.tasklet.HelloTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HelloJobConfig {

	@Bean
	public Job helloJob(
		JobBuilderFactory jobBuilderFactory,
		StepBuilderFactory stepBuilderFactory
	) {
		var transactionManager = new ResourcelessTransactionManager();
		Step helloStep = stepBuilderFactory.get("helloStep")
			.tasklet(new HelloTasklet())
			.transactionManager(transactionManager)
			.build();

		return jobBuilderFactory.get("helloJob")
			.start(helloStep)
			.build();
	}
}
