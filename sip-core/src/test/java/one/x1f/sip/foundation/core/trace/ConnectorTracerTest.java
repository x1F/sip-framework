package one.x1f.sip.foundation.core.trace;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import one.x1f.sip.foundation.core.CoreTestApplication;
import one.x1f.sip.foundation.core.apps.declarative.ConnectorProcessorExtensionsAdapter;
import org.apache.camel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(
    classes = {CoreTestApplication.class, ConnectorProcessorExtensionsAdapter.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "camel.rest.binding-mode=auto",
      "camel.openapi.enabled=false",
      "sip.core.tracing.enabled=true",
      "sip.core.tracing.logging.connector=true",
      "sip.core.tracing.log=false"
    })
@DirtiesContext
class ConnectorTracerTest {

  @Autowired private ProducerTemplate producerTemplate;
  ListAppender<ILoggingEvent> listAppender;

  @BeforeEach
  void setUp() {

    Logger logger =
        (Logger) LoggerFactory.getLogger("one.x1f.sip.foundation.core.trace.ConnectorTracer");
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
  }

  @Test
  void When_traceBeforeRoute_With_NoIdInHeaders_Then_OneTracingId() {
    // arrange
    List<ILoggingEvent> logsList = listAppender.list;

    // act
    producerTemplate.requestBody(
        "direct:" + ConnectorProcessorExtensionsAdapter.INBOUND_DIRECT_OK, "start", String.class);
    // assert
    assertThat(logsList).isNotEmpty();
  }
}
