package com.example.chunkerrorlistener;

import java.util.List;

import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemStream;

/**
 * Reads a fixed list. Resets in {@code open()} so that the same singleton step can be run
 * more than once in a test class.
 */
public class NumberReader implements ItemReader<Integer>, ItemStream {

	private final List<Integer> items;

	private int index;

	public NumberReader(List<Integer> items) {
		this.items = items;
	}

	@Override
	public void open(ExecutionContext executionContext) {
		this.index = 0;
	}

	@Override
	public Integer read() {
		return (this.index < this.items.size()) ? this.items.get(this.index++) : null;
	}

}
