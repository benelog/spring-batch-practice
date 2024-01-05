package kr.co.wikibook.batch.logbatch;

import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.core.io.WritableResource;

public class UserAccessSummaryComponents {
  public static FlatFileItemWriter<UserAccessSummary> buildCsvWriter(WritableResource resource) {
    var writer =  new FlatFileItemWriterBuilder<UserAccessSummary>()
        .name("userAccessSummaryCsvWriter")
        .resource(resource)
        .formatted()
        .format("%s,%d")
        .fieldExtractor(new UserAccessSummaryFieldSetExtractor())
        .build();
    return Configs.afterPropertiesSet(writer);
  }
}
