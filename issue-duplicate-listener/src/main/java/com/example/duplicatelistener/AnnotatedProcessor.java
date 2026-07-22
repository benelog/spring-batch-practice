package com.example.duplicatelistener;

import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.listener.ItemProcessListener;
import org.springframework.batch.infrastructure.item.ItemProcessor;

/**
 * Implements a {@code StepListener} sub-interface <em>and</em> carries a listener
 * annotation. Both facts are discovered separately by
 * {@code ChunkOrientedStepBuilder#addAsStreamAndListener}, so this object is registered
 * twice and every {@code ItemProcessListener} callback fires twice.
 */
public class AnnotatedProcessor implements ItemProcessor<Integer, Integer>, ItemProcessListener<Integer, Integer> {

	@BeforeStep
	public void beforeStep() {
		CallLog.add("beforeStep");
	}

	@Override
	public Integer process(Integer item) {
		return item;
	}

	@Override
	public void beforeProcess(Integer item) {
		CallLog.add("beforeProcess:" + item);
	}

	@Override
	public void afterProcess(Integer item, Integer result) {
		CallLog.add("afterProcess:" + item);
	}

}
