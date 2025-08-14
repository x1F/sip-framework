package one.x1f.sip.foundation.testkit.workflow.whenphase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;
import one.x1f.sip.foundation.core.proxies.ProcessorProxy;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.RouteInvoker;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExtendedCamelContext;
import org.apache.camel.builder.ExchangeBuilder;
import org.junit.jupiter.api.Test;

class ExecutionWrapperTest {

  private static final String TEST_NAME = "test";

  private CamelContext camelContext;

  @Test
  void GIVEN_mockExchangeWithProperties_WHEN_execute_THEN_validateTestKitHeaders() {
    // arrange
    camelContext = mock(CamelContext.class);
    when(camelContext.getCamelContextExtension()).thenReturn(mock(ExtendedCamelContext.class));
    Exchange inputExchange = createEmptyExchange();
    RouteInvoker routeInvoker = mock(RouteInvoker.class);
    ExecutionWrapper subject = new ExecutionWrapper(TEST_NAME, inputExchange, routeInvoker);
    when(routeInvoker.invoke(any(Exchange.class))).thenReturn(Optional.of(inputExchange));

    // act
    Optional<Exchange> actual = subject.execute();

    // assert
    assertThat(actual).isPresent();
    assertThat(actual.get().getProperty(RouteInvoker.TEST_NAME_HEADER)).isEqualTo(TEST_NAME);
    assertThat(actual.get().getProperty(ProcessorProxy.TEST_MODE_HEADER, Boolean.class)).isTrue();
    assertThat(actual.get().getMessage().getBody()).isNull();
  }

  @Test
  void GIVEN_mockExchangeWithHeaders_WHEN_execute_THEN_validateTestKitHeaders() {
    // arrange
    camelContext = mock(CamelContext.class);
    when(camelContext.getCamelContextExtension()).thenReturn(mock(ExtendedCamelContext.class));
    Exchange inputExchange = createEmptyExchange();
    RouteInvoker routeInvoker = mock(RouteInvoker.class);
    ExecutionWrapper subject = new ExecutionWrapper(TEST_NAME, inputExchange, routeInvoker);
    when(routeInvoker.invoke(any(Exchange.class))).thenReturn(Optional.of(inputExchange));

    // act
    Optional<Exchange> actual = subject.execute();

    // assert
    assertThat(actual).isPresent();
    assertThat(actual.get().getMessage().getHeader(RouteInvoker.TEST_NAME_HEADER))
        .isEqualTo(TEST_NAME);
    assertThat(actual.get().getMessage().getHeader(ProcessorProxy.TEST_MODE_HEADER, Boolean.class))
        .isTrue();
    assertThat(actual.get().getMessage().getBody()).isNull();
  }

  private Exchange createEmptyExchange() {
    ExchangeBuilder exchangeBuilder = ExchangeBuilder.anExchange(camelContext);
    return exchangeBuilder.build();
  }
}
