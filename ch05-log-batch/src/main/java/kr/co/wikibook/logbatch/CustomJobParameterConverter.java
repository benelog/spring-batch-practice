package kr.co.wikibook.logbatch;

import java.awt.Color;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CustomJobParameterConverter extends DefaultJobParametersConverter {
  public CustomJobParameterConverter() {
    super.conversionService.addConverter((Converter<String, Color>) source -> Color.decode(source));
  }
}
