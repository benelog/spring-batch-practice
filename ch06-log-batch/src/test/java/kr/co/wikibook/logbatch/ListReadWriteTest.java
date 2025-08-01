package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.item.support.ListItemWriter;

class ListReadWriteTest {

  @Test
  void read() {
    var reader = new ListItemReader<>(List.of(1, 2));
    assertThat(reader.read()).isEqualTo(1);
    assertThat(reader.read()).isEqualTo(2);
    assertThat(reader.read()).isNull();
  }


  @Test
  void write() throws Exception {
    var writer = new ListItemWriter<Integer>();
    List<Integer> items = List.of(1, 2, 3);
    writer.write(new Chunk<>(items));
    assertThat(writer.getWrittenItems()).isEqualTo(items);
  }
}
