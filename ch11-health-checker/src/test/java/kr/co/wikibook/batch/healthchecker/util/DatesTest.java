package kr.co.wikibook.batch.healthchecker.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class DatesTest {
  @ParameterizedTest
  @MethodSource("provideHolidays")
  void isHoliday(LocalDate day) {
    boolean actual = Dates.isHoliday(day);
    assertThat(actual).isTrue();
  }

  @ParameterizedTest
  @MethodSource("provideWorkingDays")
  void isWorkingDay(LocalDate day) {
    boolean actual = Dates.isHoliday(day);
    assertThat(actual).isFalse();
  }

  static Stream<LocalDate> provideHolidays() {
    return Stream.of(
        LocalDate.of(2024, 4, 6), // 토요일
        LocalDate.of(2024, 4, 28), // 일요일
        LocalDate.of(2024, 12, 25), // 크리스마스
        LocalDate.of(2024, 5, 5) // 어린이날
    );
  }

  static Stream<LocalDate> provideWorkingDays() {
    return Stream.of(
        LocalDate.of(2024, 4, 8),
        LocalDate.of(2024, 4, 15),
        LocalDate.of(2024, 4, 22)
    );
  }
}
