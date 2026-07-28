package com.example.chunkerrorlistener;

import org.springframework.batch.core.annotation.AfterChunkError;
import org.springframework.batch.core.scope.context.ChunkContext;

/**
 * The Spring Batch 5 spelling. {@code StepListenerMetaData.AFTER_CHUNK_ERROR} still maps
 * this annotation to {@code ChunkListener#afterChunkError(ChunkContext)}, which
 * {@code ChunkOrientedStep} never calls.
 */
public class LegacyAnnotatedChunkListener {

	@AfterChunkError
	public void afterChunkError(ChunkContext context) {
		CallLog.add("legacy:afterChunkError");
	}

}
