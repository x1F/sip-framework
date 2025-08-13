package one.x1f.sip.foundation.core.trace;

import static one.x1f.sip.foundation.core.trace.CustomTracer.TRACE_SET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import one.x1f.sip.foundation.core.CoreTestApplication;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.NamedNode;
import org.apache.camel.NamedRoute;
import org.apache.camel.builder.ExchangeBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(
    classes = CoreTestApplication.class,
    properties = {"sip.core.tracing.enabled=true", "sip.core.tracing.log=true"})
@DirtiesContext
class CustomTracerTest {

  private static final String EXCHANGE_ID = "exchangeId";
  private static final String ROUTE_ID = "routeId";
  @Autowired CustomTracer subject;
  @Autowired CamelContext camelContext;
  @Autowired SIPTraceConfig sipTraceConfig;
  ListAppender<ILoggingEvent> listAppender;
  Exchange exchange;
  NamedNode node = mock(NamedNode.class, RETURNS_DEEP_STUBS);

  @BeforeEach
  void setUp() {
    exchange = ExchangeBuilder.anExchange(camelContext).build();
    exchange.setExchangeId(EXCHANGE_ID);

    Logger logger = (Logger) LoggerFactory.getLogger("org.apache.camel.Tracing");
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
  }

  @Test
  void When_traceBeforeNode_With_LogsEnabled_Then_messageInLog() {
    // arrange
    List<ILoggingEvent> logsList = listAppender.list;

    // act
    subject.traceBeforeNode(node, exchange);

    // assert
    assertThat(logsList.get(0).getMessage()).contains("Id: " + EXCHANGE_ID);
    assertThat(logsList.get(0).getLevel()).isEqualTo(Level.INFO);
  }

  @Test
  void When_dumpTrace_With_DisabledLogs_Then_emptyLogs() {
    // arrange
    List<ILoggingEvent> logsList = listAppender.list;
    sipTraceConfig.setLog(false);

    // act
    subject.traceBeforeNode(node, exchange);

    // assert
    assertThat(logsList).isEmpty();
  }

  @Test
  void When_traceBeforeNode_With_NoIdInHeaders_Then_OneTracingId() {
    // act
    subject.traceBeforeNode(node, exchange);

    // assert
    assertThat(exchange.getIn().getHeader(TRACE_SET, TraceSet.class).getExchangeIds())
        .containsExactly(EXCHANGE_ID);
  }

  @Test
  void When_traceBeforeNode_With_TracingIdExists_Then_concatNewTracingId() {
    // arrange
    String oldId = "old";
    TraceSet traceSet = new TraceSet();
    exchange.getIn().setHeader(TRACE_SET, traceSet.cloneAndAdd(oldId));

    // act
    subject.traceBeforeNode(node, exchange);

    // assert
    assertThat(exchange.getIn().getHeader(TRACE_SET, TraceSet.class).getExchangeIds())
        .containsExactly(oldId, EXCHANGE_ID);
  }

  @Test
  void When_traceBeforeRoute_With_NoIdInHeaders_Then_OneTracingId() {
    // arrange
    NamedRoute namedRoute = mock(NamedRoute.class);
    when(namedRoute.getRouteId()).thenReturn(ROUTE_ID);

    // act
    subject.traceBeforeRoute(namedRoute, exchange);

    // assert
    assertThat(exchange.getIn().getHeader(TRACE_SET, TraceSet.class).getExchangeIds())
        .containsExactly(EXCHANGE_ID);
  }
}
