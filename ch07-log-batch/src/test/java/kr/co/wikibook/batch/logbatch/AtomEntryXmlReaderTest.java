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
    var resource = new ClassPathResource("blog.atom");
    var jobConfig = new CollectBlogPostJobConfig();
    StaxEventItemReader<AtomEntry> reader = jobConfig.atomEntryXmlReader(resource);
    reader.afterPropertiesSet(); // <3>

    // when
    reader.open(new ExecutionContext());
    AtomEntry item = reader.read();
    reader.close();

    // then <4>
    assertThat(item.getTitle()).startsWith("네이버에는 테크 월드를");
    assertThat(item.getLink().getHref()).isEqualTo("https://d2.naver.com/news/4029141");
    assertThat(item.getUpdated()).isEqualTo(Instant.parse("2023-12-22T14:26:03Z"));
    assertThat(item.getContent()).isNotBlank();
  }
}
