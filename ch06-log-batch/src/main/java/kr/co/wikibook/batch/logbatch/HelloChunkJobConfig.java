package kr.co.wikibook.batch.logbatch;

import java.util.PrimitiveIterator;
import java.util.stream.IntStream;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.resource.StepExecutionSimpleCompletionPolicy;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HelloChunkJobConfig {

  public static final String JOB_NAME = "helloChunkJob";

  @Bean
  public Job helloChunkJob(JobRepository jobRepository) {
    var transactionManager = new ResourcelessTransactionManager();
    var completionPolicy = new StepExecutionSimpleCompletionPolicy();
    completionPolicy.setKeyName("chunkSize");

    return new JobBuilder(JOB_NAME, jobRepository)
        .start(new StepBuilder("printSequence", jobRepository)
            .<Integer, Integer>chunk(completionPolicy, transactionManager)
            .reader(sequenceReader(1, 10))
            .processor(plus10Processor())
            .writer(consoleWriter())
            .stream(new HelloItemStream())
            .listener(completionPolicy)
            .build())
        .build();
  }

  ItemReader<Integer> sequenceReader(int from, int to) {
    IntStream itemRange = IntStream.range(from, to + 1);
    PrimitiveIterator.OfInt iterator = itemRange.iterator();
    return new IteratorItemReader<>(iterator);

  }

  ItemProcessor<Integer, Integer> plus10Processor() {
    return (item) -> item + 10;
  }

  ItemWriter<Integer> consoleWriter() {
    return System.out::println;
  }
}
