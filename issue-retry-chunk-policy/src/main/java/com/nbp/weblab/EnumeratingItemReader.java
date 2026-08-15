package com.nbp.weblab;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemStreamSupport;
import org.springframework.stereotype.Component;

/**
 * {@link ItemReader} with hard-coded input data.
 */

@Component("reader")
public class EnumeratingItemReader extends ItemStreamSupport implements ItemStreamReader<String> {
	private String[] input = {"1", "2", "3", "4", "5", "6"};
	
	private int index = 0;
	
	/**
	 * Reads next record from input
	 */
	public String read() throws Exception {
		if (index < input.length) {
			return input[index++];
		}
		else {
			return null;
		}
		
	}
	
	@Override
	public void open(ExecutionContext executionContext) {
		index = 0;
	}
}
