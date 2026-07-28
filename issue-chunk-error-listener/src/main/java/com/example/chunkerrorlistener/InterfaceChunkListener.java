package com.example.chunkerrorlistener;

import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.infrastructure.item.Chunk;

/** Control group: the same three callbacks, declared through the interface. */
public class InterfaceChunkListener implements ChunkListener<Integer, Integer> {

	@Override
	public void beforeChunk(Chunk<Integer> chunk) {
		CallLog.add("interface:beforeChunk");
	}

	@Override
	public void afterChunk(Chunk<Integer> chunk) {
		CallLog.add("interface:afterChunk");
	}

	@Override
	public void onChunkError(Exception exception, Chunk<Integer> chunk) {
		CallLog.add("interface:onChunkError");
	}

}
