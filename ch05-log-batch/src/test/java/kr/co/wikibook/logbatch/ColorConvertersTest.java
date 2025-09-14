package kr.co.wikibook.logbatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.Color;
import org.junit.jupiter.api.Test;

class ColorConvertersTest {

  @Test
  void convertStringToColor() {
    var converter = new ColorConverters.StringToColorConverter();
    String hex = "#FF0000";
    Color color = converter.convert(hex);
    assertThat(color).isEqualTo(Color.RED);
  }

  @Test
  void convertColorToString() {
    var converter = new ColorConverters.ColorToStringConverter();
    Color color = Color.BLUE;
    String hex = converter.convert(color);
    assertThat(hex).isEqualTo("#0000FF");
  }

  @Test
  void convertInvalidFormatString() {
    var converter = new ColorConverters.StringToColorConverter();
    String hex = "#00KK";
    assertThatThrownBy(() -> converter.convert(hex))
        .isInstanceOf(NumberFormatException.class)
        .hasMessage("For input string: \"00KK\" under radix 16");
  }
}
