package probe;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.builder.ChunkOrientedStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootApplication
public class ProbeApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProbeApplication.class, args).close();
  }

  static final List<String> LOG = new CopyOnWriteArrayList<>();
  static final AtomicInteger PROCESS_COUNT = new AtomicInteger();

  static class RecordingChunkListener implements ChunkListener<String, Integer> {
    @Override public void beforeChunk(Chunk<String> chunk) {
      LOG.add("beforeChunk" + types(chunk));
    }
    @Override public void afterChunk(Chunk<Integer> chunk) {
      LOG.add("afterChunk" + types(chunk));
    }
    @Override public void onChunkError(Exception e, Chunk<Integer> chunk) {
      LOG.add("onChunkError" + types(chunk));
    }
    private static String types(Chunk<?> chunk) {
      StringBuilder sb = new StringBuilder("(");
      for (Object o : chunk) sb.append(o.getClass().getSimpleName()).append(':').append(o).append(' ');
      return sb.toString().trim() + ")";
    }
  }

  static class RecordingSkipListener implements SkipListener<String, Integer> {
    @Override public void onSkipInWrite(Integer item, Throwable t) {
      LOG.add("onSkipInWrite(" + item + ", " + t.getClass().getSimpleName() + ")");
    }
  }

  static class ItemInvalid extends RuntimeException {
    ItemInvalid(String m) { super(m); }
  }

  @Bean
  CommandLineRunner runner(JobOperator jobOperator, JobRepository jobRepository, PlatformTransactionManager tm) {
    return args -> {
      run(jobOperator, jobRepository, tm, "default        ", false, false);
      run(jobOperator, jobRepository, tm, "nonTransactional", true, false);
      run(jobOperator, jobRepository, tm, "concurrent     ", false, true);
    };
  }

  private void run(JobOperator jobOperator, JobRepository jobRepository, PlatformTransactionManager tm,
      String label, boolean nonTransactional, boolean concurrent) throws Exception {
    LOG.clear();
    PROCESS_COUNT.set(0);
    ChunkOrientedStepBuilder<String, Integer> b = new StepBuilder("step-" + label.trim(), jobRepository)
        .<String, Integer>chunk(3)
        .transactionManager(tm)
        .reader(new ListItemReader<>(List.of("1", "2", "3")))
        .processor(s -> { PROCESS_COUNT.incrementAndGet(); return Integer.parseInt(s); })
        .writer(chunk -> {
          for (Integer i : chunk) if (i == 2 || i == 3) throw new ItemInvalid("item " + i);
        })
        .faultTolerant()
        .skip(ItemInvalid.class).skipLimit(10)
        .listener(new RecordingChunkListener())
        .listener(new RecordingSkipListener());
    if (nonTransactional) b.processorNonTransactional();
    if (concurrent) b.taskExecutor(new SimpleAsyncTaskExecutor("probe-"));
    Step step = b.build();
    Job job = new JobBuilder("job-" + label.trim(), jobRepository).start(step).build();
    JobExecution je = jobOperator.start(job, new JobParametersBuilder().addLong("t", System.nanoTime()).toJobParameters());
    StepExecution se = je.getStepExecutions().iterator().next();
    System.out.println("[PROBE] " + label + " status=" + je.getStatus() + " read=" + se.getReadCount()
        + " write=" + se.getWriteCount() + " writeSkip=" + se.getWriteSkipCount()
        + " rollback=" + se.getRollbackCount() + " processCalls=" + PROCESS_COUNT.get());
    for (String l : LOG) System.out.println("[PROBE]     " + l);
  }
}
