package kr.co.wikibook.batch.healthchecker.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.Chunk;

public class StepLogListener<T, S> extends StepListenerSupport<T, S> { // <1>

  private final Logger logger = LoggerFactory.getLogger(StepLogListener.class);

  @Override
  public void beforeStep(StepExecution stepExecution) {
    logger.info("beforeStep: {}", stepExecution);
  }

  @Override
  public void beforeChunk(ChunkContext context) {
    logger.info("beforeChunk: {}", context);
  }

  @Override
  public void beforeRead() {
    logger.info("beforeRead");
  }

  @Override
  public void afterRead(T item) {
    logger.info("afterRead. item={}", item);
  }

  @Override
  public void beforeProcess(T item) {
    logger.info("beforeProcess. item={}", item);
  }

  @Override
  public void afterProcess(T item, S result) {
    logger.info("afterProcess. {} -> {}", item, result);
  }

  @Override
  public void beforeWrite(Chunk<? extends S> items) {
    logger.info("beforeWrite. items={}", items);
  }

  @Override
  public void afterWrite(Chunk<? extends S> items) {
    logger.info("afterWrite. items={}", items);
  }

  @Override
  public void afterChunk(ChunkContext context) {
    logger.info("afterChunk");
  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    logger.info("afterStep: {}", stepExecution);
    return ExitStatus.COMPLETED;
  }
}
