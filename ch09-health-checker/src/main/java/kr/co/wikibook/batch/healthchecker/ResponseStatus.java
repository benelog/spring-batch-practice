package kr.co.wikibook.batch.healthchecker;

import java.net.URI;

public record ResponseStatus(
	URI url,
	int statusCode,
	long responseTimeMillis
) {
}
