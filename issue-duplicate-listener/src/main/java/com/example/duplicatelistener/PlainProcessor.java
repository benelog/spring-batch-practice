package com.example.duplicatelistener;

import org.springframework.batch.core.listener.ItemProcessListener;
import org.springframework.batch.infrastructure.item.ItemProcessor;

/** Control case: identical to {@link AnnotatedProcessor} minus the annotated method. */
public class PlainProcessor implements ItemProcessor<Integer, Integer>, ItemProcessListener<Integer, Integer> {

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
