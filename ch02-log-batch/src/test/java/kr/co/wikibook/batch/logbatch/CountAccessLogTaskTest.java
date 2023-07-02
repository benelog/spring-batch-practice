package kr.co.wikibook.batch.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

class CountAccessLogTaskTest {

  @Test
  void countAccessLog() {
    // given
    DataSource dataSource = buildDataSource();
    var notificationService = new MockNotificationService();
    var task = new CountAccessLogTask(dataSource, notificationService);

    // when
    task.run();

    // then
    String message = notificationService.getLastMessage();
    assertThat(message).isEqualTo("access_log 테이블의 건 수 : 0");
  }

  private DataSource buildDataSource() {
    return new EmbeddedDatabaseBuilder()
        .setName("log-test-db")
        .setType(EmbeddedDatabaseType.H2)
        .addScript("schema.sql")
        .build();
  }
}
