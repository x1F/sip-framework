package de.ikor.sip.foundation.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.camel.Processor;
import org.apache.camel.builder.ExpressionBuilder;
import org.apache.camel.processor.Enricher;
import org.apache.camel.processor.PollEnricher;
import org.apache.camel.processor.SendDynamicProcessor;
import org.apache.camel.processor.WireTapProcessor;
import org.junit.jupiter.api.Test;

class CamelProcessorsHelperTest {

  private static final String ENDPOINT_URI = "endpointUri";
  private static final String ENDPOINT_IN_MEMORY_URI = "sipmc:endpointUri";
  private static final String EXPRESSION_VALUE = "expression";

  @Test
  void GIVEN_specificProcessorsWithEndpoints_WHEN_isEndpointProcessor_then_expectTrue() {
    // arrange
    Processor enrichProcessor =
        new Enricher(ExpressionBuilder.simpleExpression(ENDPOINT_URI), ENDPOINT_URI);
    Processor pollEnrichProcessor = new PollEnricher(ENDPOINT_URI, 0);
    WireTapProcessor wireTapProcessor = mock(WireTapProcessor.class);
    when(wireTapProcessor.getUri()).thenReturn(ENDPOINT_URI);

    Processor sendDynamicProcessor =
        new SendDynamicProcessor(
            ENDPOINT_URI, ExpressionBuilder.simpleExpression(EXPRESSION_VALUE));

    // act & assert
    assertThat(CamelProcessorsHelper.isEndpointProcessor(enrichProcessor)).isTrue();
    assertThat(CamelProcessorsHelper.isEndpointProcessor(pollEnrichProcessor)).isTrue();
    assertThat(CamelProcessorsHelper.isEndpointProcessor(wireTapProcessor)).isTrue();
    assertThat(CamelProcessorsHelper.isEndpointProcessor(sendDynamicProcessor)).isTrue();
  }

  @Test
  void GIVEN_specificProcessorsWithInMemoryEndpoints_WHEN_isEndpointProcessor_then_expectFalse() {
    // arrange
    Processor enrichProcessor =
        new Enricher(
            ExpressionBuilder.simpleExpression(ENDPOINT_IN_MEMORY_URI), ENDPOINT_IN_MEMORY_URI);
    Processor pollEnrichProcessor =
        new PollEnricher(
            ExpressionBuilder.simpleExpression(ENDPOINT_IN_MEMORY_URI), ENDPOINT_IN_MEMORY_URI, 0);
    WireTapProcessor wireTapProcessor = mock(WireTapProcessor.class);
    when(wireTapProcessor.getUri()).thenReturn(ENDPOINT_IN_MEMORY_URI);

    Processor sendDynamicProcessor =
        new SendDynamicProcessor(
            ENDPOINT_IN_MEMORY_URI, ExpressionBuilder.simpleExpression(EXPRESSION_VALUE));

    // act & assert
    assertThat(CamelProcessorsHelper.isEndpointProcessor(enrichProcessor)).isFalse();
    assertThat(CamelProcessorsHelper.isEndpointProcessor(pollEnrichProcessor)).isFalse();
    assertThat(CamelProcessorsHelper.isEndpointProcessor(wireTapProcessor)).isFalse();
    assertThat(CamelProcessorsHelper.isEndpointProcessor(sendDynamicProcessor)).isFalse();
  }

  @Test
  void
      GIVEN_specificProcessorsWithEndpointsAndWithoutExpressions_WHEN_isEndpointProcessor_then_expectTrue() {
    // arrange
    Processor enrichProcessor = new Enricher(null, null);
    Processor pollEnrichProcessor = new PollEnricher(null, 0);

    // act & assert
    assertThat(CamelProcessorsHelper.isEndpointProcessor(enrichProcessor)).isTrue();
    assertThat(CamelProcessorsHelper.isEndpointProcessor(pollEnrichProcessor)).isTrue();
  }
}
