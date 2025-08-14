package one.x1f.sip.foundation.core.proxies;

import static one.x1f.sip.foundation.core.proxies.ProcessorProxy.TEST_MODE_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.function.UnaryOperator;
import one.x1f.sip.foundation.core.proxies.extension.ProxyExtension;
import org.apache.camel.*;
import org.apache.camel.processor.SendProcessor;
import org.apache.camel.processor.WrapProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProcessorProxyTest {

  private static final String PROXY_ID = "proxy";

  ProcessorProxy processorProxySubject;
  ProcessorProxy processorProxySubjectOutgoing;
  List<ProxyExtension> proxyExtensions;
  NamedNode namedNode;
  Processor processor;
  ProxyExtension proxyExtension;
  AsyncCallback callback;
  Exchange exchange;
  SendProcessor outgoingProcessor;
  Endpoint outgoingEndpoint;

  @BeforeEach
  void setup() {
    namedNode = mock(NamedNode.class);
    processor = mock(Processor.class);
    proxyExtension = mock(ProxyExtension.class);
    proxyExtensions = List.of(proxyExtension);
    callback = mock(AsyncCallback.class);
    exchange = mock(Exchange.class, RETURNS_DEEP_STUBS);
    processorProxySubject = new ProcessorProxy(namedNode, processor, proxyExtensions);
    outgoingProcessor = mock(SendProcessor.class);
    outgoingEndpoint = mock(Endpoint.class);
    when(outgoingProcessor.getEndpoint()).thenReturn(outgoingEndpoint);
    when(outgoingEndpoint.getEndpointBaseUri()).thenReturn("endpointUri");
    processorProxySubjectOutgoing =
        new ProcessorProxy(namedNode, outgoingProcessor, proxyExtensions);
  }

  private void putProxyInTestMode() {
    when(exchange.getProperty(TEST_MODE_HEADER, String.class)).thenReturn("true");
  }

  @Test
  void process_executeProcessExchange() throws Exception {
    // arrange
    when(namedNode.getId()).thenReturn(PROXY_ID);
    when(proxyExtension.isApplicable(any(), any(), any())).thenReturn(false);

    // act
    assertThatCode(() -> processorProxySubject.process(exchange, callback))
        .doesNotThrowAnyException();

    // assert
    verify(proxyExtension, times(1)).isApplicable(any(), any(), any());
    verify(proxyExtension, times(0)).run(any(), any(), any());
    verify(processor, times(1)).process(exchange);
  }

  @Test
  void WHEN_executeMock_THEN_setMockFunction() throws Exception {
    // arrange
    putProxyInTestMode();

    when(exchange.getPattern()).thenReturn(ExchangePattern.InOut);

    @SuppressWarnings("unchecked")
    UnaryOperator<Exchange> mockFunction = mock(UnaryOperator.class);
    when(mockFunction.apply(exchange)).thenReturn(exchange);

    // act
    processorProxySubject.mock(mockFunction);

    assertThatCode(() -> processorProxySubject.process(exchange, callback))
        .doesNotThrowAnyException();

    // assert
    verify(mockFunction, times(1)).apply(exchange);
    verify(processor, times(0)).process(exchange);
  }

  @Test
  void WHEN_doingTestMode_THEN_executeProcessExchangeInTestMode() throws Exception {
    // arrange
    putProxyInTestMode();
    when(exchange.getPattern()).thenReturn(ExchangePattern.InOut);

    // act
    assertThatCode(() -> processorProxySubject.process(exchange, callback))
        .doesNotThrowAnyException();

    // assert
    verify(processor, times(1)).process(exchange);
  }

  @Test
  void WHEN_havingProxyExtension_THEN_executeProcessExtension() throws Exception {
    // arrange
    when(namedNode.getId()).thenReturn(PROXY_ID);
    when(proxyExtension.isApplicable(any(), any(), any())).thenReturn(true);

    // act
    assertThatCode(() -> processorProxySubject.process(exchange, callback))
        .doesNotThrowAnyException();

    // assert
    verify(processor, times(1)).process(exchange);
    verify(proxyExtension, times(1)).isApplicable(any(), any(), any());
    verify(proxyExtension, times(1)).run(any(), any(), any());
  }

  @Test
  void WHEN_doingTestModeWithoutMock_THEN_endpointProcessorTestModeAfterRemovingMockFunction()
      throws Exception {
    // arrange
    putProxyInTestMode();
    when(exchange.getPattern()).thenReturn(ExchangePattern.InOut);
    @SuppressWarnings("unchecked")
    UnaryOperator<Exchange> mockFunction = mock(UnaryOperator.class);
    when(mockFunction.apply(exchange)).thenReturn(exchange);

    // act
    processorProxySubjectOutgoing.mock(mockFunction);
    processorProxySubjectOutgoing.reset();

    assertThatCode(() -> processorProxySubjectOutgoing.process(exchange, callback))
        .doesNotThrowAnyException();

    // assert
    verify(outgoingProcessor, times(1)).process(exchange);
    verify(mockFunction, times(0)).apply(exchange);
  }

  @Test
  void WHEN_regularProcessor_THEN_expectIsEndpointProcessorFalse() {
    // assert
    assertThat(processorProxySubject.isEndpointProcessor()).isFalse();
  }

  @Test
  void WHEN_regularEndpointProcessor_THEN_expectIsEndpointProcessorTrue() {
    // arrange
    when(outgoingEndpoint.getEndpointUri()).thenReturn("file://test.txt");
    processorProxySubjectOutgoing =
        new ProcessorProxy(namedNode, outgoingProcessor, proxyExtensions);

    // assert
    assertThat(processorProxySubjectOutgoing.isEndpointProcessor()).isTrue();
  }

  @Test
  void WHEN_ignoredEndpointProcessor_THEN_expectIsEndpointProcessorFalse() {
    // arrange
    when(outgoingEndpoint.getEndpointBaseUri()).thenReturn("direct:sth");
    processorProxySubjectOutgoing =
        new ProcessorProxy(namedNode, outgoingProcessor, proxyExtensions);

    // assert
    assertThat(processorProxySubjectOutgoing.isEndpointProcessor()).isFalse();
  }

  @Test
  void WHEN_wrappedRegularEndpointProcessor_THEN_expectIsEndpointProcessorTrue() {
    // arrange
    when(outgoingEndpoint.getEndpointUri()).thenReturn("file://test.txt");
    WrapProcessor wrapProcessor = new WrapProcessor(outgoingProcessor, outgoingProcessor);
    processorProxySubjectOutgoing = new ProcessorProxy(namedNode, wrapProcessor, proxyExtensions);

    // assert
    assertThat(processorProxySubjectOutgoing.isEndpointProcessor()).isTrue();
  }
}
