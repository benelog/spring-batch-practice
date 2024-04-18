package kr.co.wikibook.batch.healthchecker.url;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class CallUrlProcessor implements ItemProcessor<String, ResponseStatus> {
	private final Logger logger = LoggerFactory.getLogger(CallUrlProcessor.class);

	private final HttpClient client = HttpClient.newBuilder().build();
	private final Duration requestTimeout;

	public CallUrlProcessor(Duration requestTimeout) {
		this.requestTimeout = requestTimeout; // <1>
	}

	@Override
	public ResponseStatus process(String rawUrl) throws IOException, InterruptedException {
		logger.info("호출 시도 : {}", rawUrl); // <2>
		URI uri = URI.create(rawUrl);
		HttpRequest request = HttpRequest.newBuilder()
			.uri(uri)
			.timeout(this.requestTimeout)
			.build();

		long startTimeMillis = System.currentTimeMillis();
		HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
		long responseTimeMillis = System.currentTimeMillis() - startTimeMillis;
		logger.info("응답 시간 : {}ms", responseTimeMillis); // <3>
		if(response.statusCode() == 404) { // <4>
			logger.warn("404 응답 : {}", rawUrl);
		}
		return new ResponseStatus(uri, response.statusCode(), responseTimeMillis);
	}
}
