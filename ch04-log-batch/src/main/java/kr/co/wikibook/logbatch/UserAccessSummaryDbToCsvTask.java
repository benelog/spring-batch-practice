package kr.co.wikibook.logbatch;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;

public class UserAccessSummaryDbToCsvTask  implements CommandLineRunner {
  private final Logger log = LoggerFactory.getLogger(UserAccessSummaryDbToCsvTask.class);

  private final UserAccessSummaryDbReader reader;
  private final UserAccessSummaryCsvWriter writer;
  private final int chunkSize;

  public UserAccessSummaryDbToCsvTask(
      UserAccessSummaryDbReader reader,
      UserAccessSummaryCsvWriter writer,
      int chunkSize) {
    this.reader = reader;
    this.writer = writer;
    this.chunkSize = chunkSize;
  }

  @Override
  public void run(String... args) throws Exception {
    int totalItems = 0;
    this.reader.open();
    this.writer.open();
    List<UserAccessSummary> chunk = new LinkedList<>();

    while (true) {
      UserAccessSummary item = this.reader.read();

      if (item == null) {
        if (chunk.size() > 0) {
          this.writer.write(chunk);
        }
        break;
      }

      totalItems++;
      chunk.add(item);
      if (chunk.size() == this.chunkSize) {
        this.writer.write(chunk);
        chunk.clear();
      }
    }

    this.reader.close();
    log.info("{}개의 항목을 DB -> CSV", totalItems);
  }

  void close() throws IOException {
    this.reader.close();
    this.writer.close();
  }
}
