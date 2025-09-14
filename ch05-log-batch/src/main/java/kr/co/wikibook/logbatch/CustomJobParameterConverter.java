package kr.co.wikibook.logbatch;

import kr.co.wikibook.logbatch.ColorConverters.StringToColorConverter;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.stereotype.Component;

@Component
public class CustomJobParameterConverter extends DefaultJobParametersConverter {
  public CustomJobParameterConverter() {
    super.conversionService.addConverter(new StringToColorConverter());
  }
}
