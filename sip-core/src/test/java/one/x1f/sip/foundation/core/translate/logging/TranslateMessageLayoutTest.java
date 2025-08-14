package one.x1f.sip.foundation.core.translate.logging;

import static one.x1f.sip.foundation.core.proxies.ProcessorProxy.TEST_MODE_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import java.time.Instant;
import one.x1f.sip.foundation.core.translate.SIPTranslateMessageService;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TranslateMessageLayoutTest {

  private static final String MESSAGE = "message";
  TranslateMessageLayout subject;
  ILoggingEvent loggingEvent;

  @BeforeEach
  void setup() {
    subject = new TranslateMessageLayout();
    subject.setPattern("%msg");
    subject.setContext(mock(LoggerContext.class));
    subject.start();
    loggingEvent = mock(LoggingEvent.class);
    when(loggingEvent.getMessage()).thenReturn(MESSAGE);
    when(loggingEvent.getInstant()).thenReturn(Instant.now());
  }

  @Test
  void When_doLayout_Then_BaseMessage() {
    // act
    String target = subject.doLayout(loggingEvent);
    // assert
    assertThat(target).isEqualTo(MESSAGE);
  }

  @Test
  void When_doLayout_With_TestMode_Then_MessageWithSipTest() {
    // arrange
    ThreadContext.put(TEST_MODE_HEADER, "true");

    // act
    String target = subject.doLayout(loggingEvent);

    // assert
    assertThat(target).isEqualTo("[SIP TEST] " + MESSAGE);

    ThreadContext.remove(TEST_MODE_HEADER);
  }

  @Test
  void When_doLayout_With_MessageService_Then_TranslatedMessage() {
    // arrange
    String translated = "translated";
    SIPTranslateMessageService sipTranslateMessageService = mock(SIPTranslateMessageService.class);
    ReflectionTestUtils.setField(subject, "messageService", sipTranslateMessageService);
    when(sipTranslateMessageService.getTranslatedMessage(any(), any())).thenReturn(translated);

    // act
    String target = subject.doLayout(loggingEvent);

    // assert
    assertThat(target).isEqualTo(translated);
  }
}
