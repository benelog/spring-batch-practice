package com.example.tracinghandler;

import java.util.List;

import io.opentelemetry.sdk.trace.data.SpanData;

/**
 * Outcome of one job run: the spans that were exported, the exit status of the job and the
 * number of {@code spring.batch.*} meters that were recorded.
 */
public record ScenarioResult(String exitStatus, String failureMessage, List<SpanData> spans, long batchMeterCount) {

	public void print(String title) {
		System.out.println("=== " + title + " ===");
		System.out.println("job exit status         = " + this.exitStatus);
		if (this.failureMessage != null) {
			System.out.println("job failure             = " + this.failureMessage);
		}
		System.out.println("finished span count     = " + this.spans.size());
		for (SpanData span : this.spans) {
			System.out.println("  span: name=" + span.getName() + " traceId=" + span.getTraceId() + " parentSpanId="
					+ span.getParentSpanId());
		}
		System.out.println("spring.batch.* meters   = " + this.batchMeterCount);
		System.out.println();
	}

}
