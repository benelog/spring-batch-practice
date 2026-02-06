package kr.co.wikibook.batch.logbatch;

import java.time.Instant;

public record AccessLog(
    Instant accessDateTime,
    String ip,
    String username
) {
}
