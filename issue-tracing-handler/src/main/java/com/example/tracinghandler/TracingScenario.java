package com.example.tracinghandler;

import java.util.List;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import io.micrometer.tracing.handler.TracingAwareMeterObservationHandler;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.ResourcelessJobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

/**
 * Runs the same two-step job twice: once with the handler shown in the reference
 * documentation and once with a handler that actually creates spans.
 */
public final class TracingScenario {

	private TracingScenario() {
	}

	/**
	 * The configuration from the reference documentation (Micrometer support / Tracing).
	 */
	public static ScenarioResult runDocumentedExample() {
		return run("documented", (observationRegistry, meterRegistry, tracer) -> observationRegistry.observationConfig()
			.observationHandler(
					new TracingAwareMeterObservationHandler<>(new DefaultMeterObservationHandler(meterRegistry),
							tracer)));
	}

	/**
	 * A {@code TracingObservationHandler} implementation, which is what creates spans.
	 */
	public static ScenarioResult runTracingObservationHandler() {
		return run("fixed", (observationRegistry, meterRegistry, tracer) -> observationRegistry.observationConfig()
			.observationHandler(new DefaultTracingObservationHandler(tracer)));
	}

	/**
	 * Metrics and tracing together, with the tracing handler registered first. This is what
	 * Spring Boot's {@code TracingAndMeterObservationHandlerGroup} ends up doing.
	 */
	public static ScenarioResult runTracingAndMetrics() {
		return run("both", (observationRegistry, meterRegistry, tracer) -> observationRegistry.observationConfig()
			.observationHandler(new DefaultTracingObservationHandler(tracer))
			.observationHandler(
					new TracingAwareMeterObservationHandler<>(new DefaultMeterObservationHandler(meterRegistry),
							tracer)));
	}

	private static ScenarioResult run(String suffix, HandlerRegistrar registrar) {
		InMemorySpanExporter spanExporter = InMemorySpanExporter.create();
		Tracer tracer = otelTracer(spanExporter);
		MeterRegistry meterRegistry = new SimpleMeterRegistry();
		ObservationRegistry observationRegistry = ObservationRegistry.create();
		registrar.register(observationRegistry, meterRegistry, tracer);

		String exitStatus;
		String failureMessage = null;
		try {
			JobExecution execution = launch(suffix, observationRegistry);
			exitStatus = execution.getExitStatus().getExitCode();
			failureMessage = execution.getAllFailureExceptions()
				.stream()
				.findFirst()
				.map(throwable -> throwable.getClass().getName() + ": " + throwable.getMessage())
				.orElse(null);
		}
		catch (Exception ex) {
			exitStatus = "LAUNCH_THREW";
			failureMessage = ex.getClass().getName() + ": " + ex.getMessage();
		}

		List<SpanData> spans = spanExporter.getFinishedSpanItems();
		long batchMeters = meterRegistry.getMeters()
			.stream()
			.filter(meter -> meter.getId().getName().startsWith("spring.batch"))
			.count();
		return new ScenarioResult(exitStatus, failureMessage, spans, batchMeters);
	}

	private static JobExecution launch(String suffix, ObservationRegistry observationRegistry) throws Exception {
		JobRepository jobRepository = new ResourcelessJobRepository();

		Step step1 = new StepBuilder("step1-" + suffix, jobRepository).observationRegistry(observationRegistry)
			.tasklet((contribution, chunkContext) -> RepeatStatus.FINISHED)
			.build();
		Step step2 = new StepBuilder("step2-" + suffix, jobRepository).observationRegistry(observationRegistry)
			.tasklet((contribution, chunkContext) -> RepeatStatus.FINISHED)
			.build();

		Job job = new JobBuilder("job-" + suffix, jobRepository).observationRegistry(observationRegistry)
			.start(step1)
			.next(step2)
			.build();

		TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
		jobLauncher.setJobRepository(jobRepository);
		jobLauncher.afterPropertiesSet();
		JobParameters jobParameters = new JobParametersBuilder().addString("run", suffix).toJobParameters();
		return jobLauncher.run(job, jobParameters);
	}

	private static Tracer otelTracer(InMemorySpanExporter spanExporter) {
		SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
			.addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
			.build();
		io.opentelemetry.api.trace.Tracer otelTracer = tracerProvider.get("issue-tracing-handler");
		return new OtelTracer(otelTracer, new OtelCurrentTraceContext(), event -> {
		});
	}

	@FunctionalInterface
	private interface HandlerRegistrar {

		void register(ObservationRegistry observationRegistry, MeterRegistry meterRegistry, Tracer tracer);

	}

}
