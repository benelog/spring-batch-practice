package com.example.tracinghandler;

import io.opentelemetry.sdk.trace.data.SpanData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TracingHandlerTests {

	@Test
	@DisplayName("the configuration shown in the reference documentation should create spans (fails)")
	void documentedExampleShouldCreateSpans() {
		ScenarioResult result = TracingScenario.runDocumentedExample();

		assertThat(result.exitStatus()).isEqualTo("COMPLETED");
		assertThat(result.spans()).hasSize(3);
	}

	@Test
	@DisplayName("a TracingObservationHandler creates one job span and one span per step (passes)")
	void tracingObservationHandlerCreatesSpans() {
		ScenarioResult result = TracingScenario.runTracingObservationHandler();

		assertThat(result.exitStatus()).isEqualTo("COMPLETED");
		assertThat(result.spans()).hasSize(3);
		assertThat(result.spans()).extracting(SpanData::getName)
			.containsExactlyInAnyOrder("spring.batch.step", "spring.batch.step", "spring.batch.job");
		assertThat(result.spans()).extracting(SpanData::getTraceId).hasSize(3).containsOnly(jobSpan(result).getTraceId());
		assertThat(result.spans())
			.filteredOn(span -> span.getName().equals("spring.batch.step"))
			.allSatisfy(span -> assertThat(span.getParentSpanId()).isEqualTo(jobSpan(result).getSpanId()));
	}

	@Test
	@DisplayName("registering both handlers yields spans and metrics (passes)")
	void tracingAndMetricsHandlersBothWork() {
		ScenarioResult result = TracingScenario.runTracingAndMetrics();

		assertThat(result.exitStatus()).isEqualTo("COMPLETED");
		assertThat(result.spans()).hasSize(3);
		assertThat(result.batchMeterCount()).isPositive();
	}

	private SpanData jobSpan(ScenarioResult result) {
		return result.spans()
			.stream()
			.filter(span -> span.getName().equals("spring.batch.job"))
			.findFirst()
			.orElseThrow();
	}

}
