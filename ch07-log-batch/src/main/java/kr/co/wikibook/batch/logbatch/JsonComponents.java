package kr.co.wikibook.batch.logbatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

public class JsonComponents {

  private static ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
      .dateFormat(new StdDateFormat())
      .build();

  public static JsonItemReader<AccessLog> buildJsonItemReader(Resource resource) {
    var objectReader = new JacksonJsonObjectReader<>(objectMapper, AccessLog.class);
    return new JsonItemReader<>(resource, objectReader);
  }

  public static JsonFileItemWriter<AccessLog> buildJsonItemWriter(WritableResource resource) {
    var objectMarshaller = new JacksonJsonObjectMarshaller<AccessLog>(objectMapper);
    return Configs.afterPropertiesSet(
        new JsonFileItemWriter<>(resource, objectMarshaller)
    );
  }
}
