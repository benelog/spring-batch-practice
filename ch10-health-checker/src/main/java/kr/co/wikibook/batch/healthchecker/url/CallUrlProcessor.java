package kr.co.wikibook.batch.healthchecker.url;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;

public class CallUrlProcessor implements
		ItemProcessor<String, ResponseStatus>,
		ItemProcessListener<String, ResponseStatus> {
	private final Logger logger = LoggerFactory.getLogger(CallUrlProcessor.class);

	private final HttpClient client = HttpClient.newBuilder().build();
	private final Duration requestTimeout;

	public CallUrlProcessor(Duration requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	@BeforeStep
	public void logRequestTimeout() {
		logger.info("requestTimeout : {} seconds", this.requestTimeout.getSeconds());
	}

	@Override
	public void beforeProcess(String rawUrl) {
		logger.info("호출 시도 : {}", rawUrl);
	}

	@Override
	public ResponseStatus process(String rawUrl) throws IOException, InterruptedException {
		URI uri = URI.create(rawUrl);
		HttpRequest request = HttpRequest.newBuilder()
			.uri(uri)
			.timeout(this.requestTimeout)
			.build();

		long startTimeMillis = System.currentTimeMillis();
		HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
		long responseTimeMillis = System.currentTimeMillis() - startTimeMillis;
		return new ResponseStatus(uri, response.statusCode(), responseTimeMillis);
	}

	@Override
	public void afterProcess(String rawUrl, ResponseStatus result) {
		if (result == null) {
			return;
		}

		logger.info("응답 시간 : {}ms", result.responseTimeMillis());
		if(result.statusCode() == 404) {
			logger.warn("404 응답 : {}", rawUrl);
		}
	}

	@Override
	public void onProcessError(String rawUrl, Exception ex) {
		logger.warn("호출 실패 : {}", rawUrl, ex); // <4>
	}
}
