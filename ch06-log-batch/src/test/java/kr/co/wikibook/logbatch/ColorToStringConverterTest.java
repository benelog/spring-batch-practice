package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import org.junit.jupiter.api.Test;

class ColorToStringConverterTest {
  @Test
  void convertColorToString() {
    var converter = new ColorToStringConverter();
    Color color = Color.BLUE;
    String hex = converter.convert(color);
    assertThat(hex).isEqualTo("#0000FF");
  }
}