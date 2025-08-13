package one.x1f.sip.foundation.core.translate.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SIPPatternLayoutEncoderTest {

  @Test
  void When_startEncoder_Expect_successfullyStarted() {
    // arrange
    SIPPatternLayoutEncoder subject = new SIPPatternLayoutEncoder();

    // act
    subject.start();

    // assign
    assertThat(ReflectionTestUtils.getField(subject, "layout").getClass())
        .isEqualTo(TranslateMessageLayout.class);
    assertThat((Boolean) ReflectionTestUtils.getField(subject, "started")).isTrue();
  }
}
