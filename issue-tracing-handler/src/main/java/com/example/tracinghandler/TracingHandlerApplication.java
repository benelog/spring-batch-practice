package com.example.tracinghandler;

/**
 * Prints the outcome of both configurations side by side.
 */
public final class TracingHandlerApplication {

	public static void main(String[] args) {
		TracingScenario.runDocumentedExample().print("TracingAwareMeterObservationHandler (reference documentation)");
		TracingScenario.runTracingObservationHandler().print("DefaultTracingObservationHandler");
	}

}
