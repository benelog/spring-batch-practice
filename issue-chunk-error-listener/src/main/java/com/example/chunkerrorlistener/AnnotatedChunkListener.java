package com.example.chunkerrorlistener;

import org.springframework.batch.core.annotation.AfterChunk;
import org.springframework.batch.core.annotation.BeforeChunk;
import org.springframework.batch.core.annotation.OnChunkError;
import org.springframework.batch.infrastructure.item.Chunk;

/**
 * Annotation-based chunk listener. The three methods use the signatures documented on the
 * annotations themselves.
 *
 * @see BeforeChunk
 * @see AfterChunk
 * @see OnChunkError
 */
public class AnnotatedChunkListener {

	@BeforeChunk
	public void beforeChunk(Chunk<Integer> chunk) {
		CallLog.add("annotation:beforeChunk");
	}

	@AfterChunk
	public void afterChunk(Chunk<Integer> chunk) {
		CallLog.add("annotation:afterChunk");
	}

	@OnChunkError
	public void onChunkError(Exception exception, Chunk<Integer> chunk) {
		CallLog.add("annotation:onChunkError");
	}

}
