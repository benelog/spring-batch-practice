package kr.co.wikibook.batch.logbatch;

import java.util.stream.IntStream;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.PassThroughFieldExtractor;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.WritableResource;

@Configuration
public class TransactionalFileJobConfig {

  public static final String JOB_NAME = "transactionalFileJob";

  @Bean
  public Job transactionalFileJob(JobRepository jobRepository) {
    var numberOutput = new PathResource("numbers.txt");

    var writer = new CompositeItemWriterBuilder<Integer>()
        .delegates(
            this.buildFlatFileItemWriter(numberOutput), // <1>
            this.buildIntentionalErrorWriter(13) // <2>
        ).build();

    return new JobBuilder(JOB_NAME, jobRepository)
        .start(new StepBuilder("generateSequenceFile", jobRepository) // <3>
            .<Integer, Integer>chunk(10, new ResourcelessTransactionManager()) // <4>
            .reader(this.buildSequenceReader(1, 30)) // <5>
            .writer(writer)
            .build())
        .build();
  }

  ItemReader<Integer> buildSequenceReader(int from, int to) {
    IntStream itemRange = IntStream.range(from, to + 1);
    return new IteratorItemReader<>(itemRange.iterator());
  }

  ItemWriter<Integer> buildIntentionalErrorWriter(int errorItem) {
    return (numbers) -> {
      for (Integer number : numbers) {
        if (number == errorItem) { // <6>
          throw new IllegalStateException("의도적인 에러");
        }
        System.out.println(number);
      }
    };
  }

  FlatFileItemWriter<Integer> buildFlatFileItemWriter(WritableResource resource) {
    return new FlatFileItemWriterBuilder<Integer>()
        .name("userAccessSummaryCsvWriter")
        .resource(resource)
        .transactional(true) // <7>
        .delimited()
        .fieldExtractor(new PassThroughFieldExtractor<>()) // <8>
        .build();
  }
}
