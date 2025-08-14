package one.x1f.sip.foundation.testkit.workflow.reporting.resultprocessor.impl;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class LogbackOutputStreamTest {

  @Test
  void When_processLine_Expect_LogInTestReportLogLogger() {
    // arrange
    ListAppender<ILoggingEvent> listAppender;
    Logger logger = (Logger) LoggerFactory.getLogger("TestReportLog");
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
    List<ILoggingEvent> logsList = listAppender.list;
    LogbackOutputStream subject = new LogbackOutputStream();
    String message = "message";

    // act

    subject.processLine(message, 1);

    // assert
    assertThat(logsList.get(0).getMessage()).isEqualTo(message);
    assertThat(logsList.get(0).getLevel()).isEqualTo(Level.ERROR);
  }
}
