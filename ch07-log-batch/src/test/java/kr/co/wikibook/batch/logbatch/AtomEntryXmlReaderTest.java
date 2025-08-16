package kr.co.wikibook.batch.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import kr.co.wikibook.batch.logbatch.atom.AtomEntry;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.core.io.ClassPathResource;

class AtomEntryXmlReaderTest {
  @Test
  void read() throws Exception {
    // given
    var resource = new ClassPathResource("d2.atom");
    var jobConfig = new CollectBlogPostJobConfig();
    StaxEventItemReader<AtomEntry> reader = jobConfig.atomEntryXmlReader(resource);
    reader.afterPropertiesSet(); // <3>

    // when
    reader.open(new ExecutionContext());
    AtomEntry item = reader.read();
    reader.close();

    // then <4>
    assertThat(item.getTitle()).startsWith("[FE Ground]");
    assertThat(item.getLink().getHref()).isEqualTo("https://d2.naver.com/news/6518915");
    assertThat(item.getUpdated()).isEqualTo(Instant.parse("2025-08-13T11:07:21Z"));
    assertThat(item.getContent()).isNotBlank();
  }
}
