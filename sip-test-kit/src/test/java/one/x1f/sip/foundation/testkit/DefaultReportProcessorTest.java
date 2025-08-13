package one.x1f.sip.foundation.testkit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.testkit.workflow.TestExecutionStatus;
import one.x1f.sip.foundation.testkit.workflow.reporting.resultprocessor.impl.DefaultReportProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@Slf4j
@ExtendWith(MockitoExtension.class)
class DefaultReportProcessorTest {

  private static final String TEMPLATE_NAME = "report-template.ftl";

  @Mock private Configuration templateConfiguration;
  @Mock private Template template;
  private DefaultReportProcessor subject;

  @Test
  void When_processTestReport_Expect_TemplateProcessCalled() throws IOException, TemplateException {
    // arrange
    when(templateConfiguration.getTemplate(TEMPLATE_NAME)).thenReturn(template);
    subject = new DefaultReportProcessor(templateConfiguration);
    TestExecutionStatus report = new TestExecutionStatus("test").setSuccessfulExecution(true);

    // act
    subject.process(report);

    // assert
    verify(template, times(1)).process(any(), any());
  }

  @Test
  void When_processTestReport_Expect_Exception() throws IOException, TemplateException {
    // arrange
    ListAppender<ILoggingEvent> listAppender;
    Logger logger = (Logger) LoggerFactory.getLogger(DefaultReportProcessor.class);
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
    List<ILoggingEvent> logsList = listAppender.list;
    String exceptionMessage = "Exception message";
    doThrow(new RuntimeException(exceptionMessage))
        .when(templateConfiguration)
        .getTemplate(TEMPLATE_NAME);
    subject = new DefaultReportProcessor(templateConfiguration);
    TestExecutionStatus report = new TestExecutionStatus("test").setSuccessfulExecution(true);

    // act
    subject.process(report);

    // assert
    assertThat(logsList.get(0).getMessage()).isEqualTo(exceptionMessage);
    assertThat(logsList.get(0).getLevel()).isEqualTo(Level.ERROR);
  }
}
