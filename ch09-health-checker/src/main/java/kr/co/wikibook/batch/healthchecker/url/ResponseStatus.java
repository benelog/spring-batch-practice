package kr.co.wikibook.batch.healthchecker.url;

import java.net.URI;

public record ResponseStatus(
	URI url,
	int statusCode,
	long responseTimeMillis
) {
}
