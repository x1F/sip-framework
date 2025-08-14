package one.x1f.sip.foundation.testkit.workflow.reporting.resultprocessor.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestKitLogbackFilterTest {

  private static final String TEST_REPORT_LOG = "TestReportLog";

  private TestKitLogbackFilter subject;
  private ILoggingEvent event;

  @BeforeEach
  void setUp() {
    event = mock(ILoggingEvent.class);
    subject = new TestKitLogbackFilter();
    subject.setLoggerName(TEST_REPORT_LOG);
    subject.setOnMatch(FilterReply.DENY);
  }

  @Test
  void When_start_With_MissingLoggerName_ExpectDoesNotThrow() {
    subject.setLoggerName(null);

    assertThatNoException().isThrownBy(() -> subject.start());
    assertThat(subject.getName()).isNull();
  }

  @Test
  void When_decide_With_FilterNotStarted_Then_NeutralReply() {
    // act + assert
    assertThat(subject.decide(event)).isEqualTo(FilterReply.NEUTRAL);
  }

  @Test
  void When_decide_With_FilterStartedAndMatches_Then_DenyReply() {
    // arrange
    when(event.getLoggerName()).thenReturn(TEST_REPORT_LOG);

    // act
    subject.start();
    FilterReply reply = subject.decide(event);

    // assert
    assertThat(reply).isEqualTo(FilterReply.DENY);
  }

  @Test
  void When_decide_WithFilterStartedAndNoMatch_Then_NeutralReply() {
    // arrange
    when(event.getLoggerName()).thenReturn("Other log");

    // act
    subject.start();
    FilterReply reply = subject.decide(event);

    // assert
    assertThat(reply).isEqualTo(FilterReply.NEUTRAL);
  }
}
