package com.example.duplicatelistener;

import java.util.ArrayList;
import java.util.List;

/** Collects the listener callbacks that were actually invoked. */
public final class CallLog {

	private static final List<String> CALLS = new ArrayList<>();

	private CallLog() {
	}

	public static void add(String call) {
		CALLS.add(call);
	}

	public static List<String> drain() {
		List<String> copy = List.copyOf(CALLS);
		CALLS.clear();
		return copy;
	}

}
