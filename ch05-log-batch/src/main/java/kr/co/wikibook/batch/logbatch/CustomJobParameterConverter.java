package kr.co.wikibook.batch.logbatch;

import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

//@Component
public class CustomJobParameterConverter extends DefaultJobParametersConverter {
  public CustomJobParameterConverter() {
    Converter<String, ReportFormat> reportFormatConverter = new Converter<>() {
      @Override
      public ReportFormat convert(String source) {
        return ReportFormat.valueOf(source.toUpperCase());
      }
    };
    super.conversionService.addConverter(reportFormatConverter);
  }
}
