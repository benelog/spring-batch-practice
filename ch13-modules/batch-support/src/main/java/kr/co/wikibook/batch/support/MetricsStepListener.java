package kr.co.wikibook.batch.support;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;

/**
 * 스텝이 끝날 때마다 쓰기·스킵 건수를 업무 지표로 남기는 공통 리스너.
 * 스프링 배치가 자체 메트릭을 남길 때처럼 마이크로미터의 전역 레지스트리를 사용하므로,
 * admin-web 같은 스프링 부트 모듈뿐 아니라 부트를 쓰지 않는 CLI 모듈에서도 그대로 동작한다.
 */
public class MetricsStepListener implements StepExecutionListener {

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    String jobName = stepExecution.getJobExecution().getJobInstance().getJobName();
    String stepName = stepExecution.getStepName();
    Counter.builder("batch.step.written") // <1>
        .tag("job", jobName)
        .tag("step", stepName)
        .description("스텝이 쓴 아이템 수")
        .register(Metrics.globalRegistry)
        .increment(stepExecution.getWriteCount()); // <2>
    Counter.builder("batch.step.skipped")
        .tag("job", jobName)
        .tag("step", stepName)
        .description("스텝이 스킵한 아이템 수")
        .register(Metrics.globalRegistry)
        .increment(stepExecution.getSkipCount());
    return stepExecution.getExitStatus(); // <3>
  }
}
