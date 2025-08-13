package one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl;

import static one.x1f.sip.foundation.core.proxies.ProcessorProxy.TEST_MODE_HEADER;
import static one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.RouteInvoker.TEST_NAME_HEADER;
import static org.apache.camel.Exchange.MESSAGE_TIMESTAMP;
import static org.apache.camel.component.kafka.KafkaConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import one.x1f.sip.foundation.testkit.util.TestKitHelper;
import one.x1f.sip.foundation.testkit.workflow.givenphase.Mock;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.exceptions.RouteInvokerException;
import org.apache.camel.*;
import org.apache.camel.component.kafka.KafkaConfiguration;
import org.apache.camel.component.kafka.KafkaConsumer;
import org.apache.camel.component.kafka.KafkaEndpoint;
import org.apache.camel.component.kafka.serde.DefaultKafkaHeaderSerializer;
import org.apache.camel.component.kafka.serde.KafkaHeaderSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KafkaRouteInvokerTest {

  private static final String ROUTE_ID = "routeId";
  private static final String TOPIC_NAME = "test_topic";

  private KafkaRouteInvoker subject;
  private Exchange inputExchange;
  private Exchange actualKafkaExchange;
  private Processor processor;

  @BeforeEach
  void setup() {
    CamelContext camelContext = mock(CamelContext.class);
    when(camelContext.getCamelContextExtension()).thenReturn(mock(ExtendedCamelContext.class));
    subject = new KafkaRouteInvoker(camelContext);

    inputExchange = TestKitHelper.parseExchangeProperties(null, camelContext);
    inputExchange.setProperty(Mock.ENDPOINT_ID_EXCHANGE_PROPERTY, ROUTE_ID);

    Route route = mock(Route.class);
    KafkaConsumer kafkaConsumer = mock(KafkaConsumer.class);
    KafkaEndpoint kafkaEndpoint = mock(KafkaEndpoint.class);
    KafkaConfiguration kafkaConfiguration = mock(KafkaConfiguration.class);
    processor = mock(Processor.class);
    when(camelContext.getRoute(ROUTE_ID)).thenReturn(route);
    when(route.getConsumer()).thenReturn(kafkaConsumer);
    when(kafkaConsumer.getEndpoint()).thenReturn(kafkaEndpoint);
    when(kafkaConsumer.getProcessor()).thenReturn(processor);
    when(kafkaEndpoint.getConfiguration()).thenReturn(kafkaConfiguration);
    when(kafkaConfiguration.getTopic()).thenReturn(TOPIC_NAME);

    actualKafkaExchange = TestKitHelper.parseExchangeProperties(null, camelContext);
    when(kafkaConsumer.createExchange(false)).thenReturn(actualKafkaExchange);
  }

  @Test
  void GIVEN_noInputs_WHEN_invoke_THEN_verifyProcessorCallAndDefaultBodyAndHeaderValues()
      throws Exception {
    // act
    subject.invoke(inputExchange);

    // assert
    verify(processor, times(1)).process(actualKafkaExchange);
    assertThat(actualKafkaExchange.getMessage().getBody()).isNull();
    assertThat(actualKafkaExchange.getMessage().getHeader(TOPIC)).isEqualTo(TOPIC_NAME);
    assertThat(actualKafkaExchange.getMessage().getHeader(TIMESTAMP)).isNotNull();
    assertThat(actualKafkaExchange.getMessage().getHeader(MESSAGE_TIMESTAMP)).isNotNull();
  }

  @Test
  void GIVEN_bodyAndCamelKafkaSpecificHeaders_WHEN_invoke_THEN_verifyBodyAndHeaderValues()
      throws Exception {
    // arrange
    inputExchange.getMessage().setBody("body value");
    inputExchange.getMessage().setHeader(TOPIC, "custom_topic");
    inputExchange.getMessage().setHeader(OFFSET, 32);
    inputExchange.getMessage().setHeader(PARTITION, 1);

    // act
    subject.invoke(inputExchange);

    // assert
    assertThat(actualKafkaExchange.getMessage().getBody()).isEqualTo("body value");
    assertThat(actualKafkaExchange.getMessage().getHeader(TOPIC)).isEqualTo("custom_topic");
    assertThat(actualKafkaExchange.getMessage().getHeader(OFFSET)).isEqualTo(32);
    assertThat(actualKafkaExchange.getMessage().getHeader(PARTITION)).isEqualTo(1);
  }

  @Test
  void GIVEN_customHeaders_WHEN_invoke_THEN_verifyHeaderValues() throws Exception {
    // arrange
    KafkaHeaderSerializer kafkaHeaderSerializer = new DefaultKafkaHeaderSerializer();
    String customHeaderValue = "test";
    inputExchange.getMessage().setHeader("customHeader", customHeaderValue);

    // act
    subject.invoke(inputExchange);

    // assert
    assertThat(actualKafkaExchange.getMessage().getHeader("customHeader"))
        .isEqualTo(kafkaHeaderSerializer.serialize("customHeader", customHeaderValue));
  }

  @Test
  void GIVEN_testKitHeadersAsCustomHeaders_WHEN_invoke_THEN_verifyHeaderValues() throws Exception {
    // arrange
    inputExchange.getMessage().setHeader(TEST_NAME_HEADER, "test1");
    inputExchange.getMessage().setHeader(TEST_MODE_HEADER, "true");

    // act
    subject.invoke(inputExchange);

    // assert
    assertThat(actualKafkaExchange.getMessage().getHeader(TEST_NAME_HEADER)).isEqualTo("test1");
    assertThat(actualKafkaExchange.getMessage().getHeader(TEST_MODE_HEADER)).isEqualTo("true");
  }

  @Test
  void GIVEN_simulatedException_WHEN_invoke_THEN_expectRouteInvokerRuntimeException()
      throws Exception {
    // arrange
    doThrow(Exception.class).when(processor).process(any());

    // act & assert
    assertThrows(RouteInvokerException.class, () -> subject.invoke(inputExchange));
  }

  @Test
  void GIVEN_noInput_WHEN_isApplicable_THEN_expectTrue() {
    // arrange
    KafkaEndpoint endpoint = mock(KafkaEndpoint.class);

    // act + assert
    assertThat(subject.isApplicable(endpoint)).isTrue();
  }

  @Test
  void GIVEN_noInput_WHEN_isApplicable_THEN_expectFalse() {
    // arrange
    Endpoint endpoint = mock(Endpoint.class);

    // act + assert
    assertThat(subject.isApplicable(endpoint)).isFalse();
  }
}
