package one.x1f.sip.foundation.testkit.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RegexUtilTest {

  @Test
  void GIVEN_compliantActual_WHEN_compare_THEN_resultTrue() {
    // arrange
    String expected = "sth.sth";
    String actual = "sthasth";
    // act
    Boolean result = RegexUtil.compare(expected, actual);
    // assert
    assertThat(result).isTrue();
  }

  @Test
  void GIVEN_actualWithCarriageReturn_WHEN_compare_THEN_resultTrue() {
    // arrange
    String expected = "\r\nsth.sth";
    String actual = "\nsthasth";
    // act
    Boolean result = RegexUtil.compare(expected, actual);
    // assert
    assertThat(result).isTrue();
  }

  @Test
  void GIVEN_nonCompliantActual_WHEN_compare_THEN_resultFalse() {
    // arrange
    String expected = "sth.sth";
    String actual = "sthsth";
    // act
    Boolean result = RegexUtil.compare(expected, actual);
    // assert
    assertThat(result).isFalse();
  }
}
