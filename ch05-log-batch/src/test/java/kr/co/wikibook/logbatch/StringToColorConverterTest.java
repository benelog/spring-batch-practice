package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.Color;
import org.junit.jupiter.api.Test;

class StringToColorConverterTest {

  StringToColorConverter converter = new StringToColorConverter();

  @Test
  void convert() {
    String hex = "#FF0000";
    Color color = converter.convert(hex);
    assertThat(color).isEqualTo(Color.RED);
  }

  @Test
  void convertInvalidFormatString() {
    String hex = "#00KK";
    assertThatThrownBy(() -> converter.convert(hex))
        .isInstanceOf(NumberFormatException.class)
        .hasMessage("For input string: \"00KK\" under radix 16");
  }
}
