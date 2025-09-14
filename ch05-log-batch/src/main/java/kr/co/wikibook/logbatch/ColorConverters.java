package kr.co.wikibook.logbatch;

import java.awt.Color;
import org.springframework.core.convert.converter.Converter;

public class ColorConverters {

  static class StringToColorConverter implements Converter<String, Color> {

    @Override
    public Color convert(String hex) {
      return Color.decode(hex);
    }
  }

  static class ColorToStringConverter implements Converter<Color, String> {

    @Override
    public String convert(Color color) {
      return String.format(
          "#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()
      );
    }
  }
}
