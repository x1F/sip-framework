package one.x1f.sip.foundation.testkit.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import one.x1f.sip.foundation.core.proxies.ProcessorProxy;
import one.x1f.sip.foundation.core.proxies.ProcessorProxyRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ProcessorProxyTestPreConditionerTest {

  @Test
  void setDefaultMockOnAllEndpointProcessors() {
    // arrange
    ProcessorProxy endpointProxy = mock(ProcessorProxy.class, CALLS_REAL_METHODS);
    ProcessorProxy nonEndpointProxy = mock(ProcessorProxy.class, CALLS_REAL_METHODS);
    when(endpointProxy.isEndpointProcessor()).thenReturn(true);
    when(nonEndpointProxy.isEndpointProcessor()).thenReturn(false);
    ProcessorProxyRegistry proxyRegistry = new ProcessorProxyRegistry();
    proxyRegistry.register("endpoint", endpointProxy);
    proxyRegistry.register("nonEndpoint", nonEndpointProxy);
    ProcessorProxyTestPreConditioner subject = new ProcessorProxyTestPreConditioner(proxyRegistry);

    // act
    subject.setDefaultMockOnAllEndpointProcessors();

    // assert
    assertThat(ReflectionTestUtils.getField(endpointProxy, "mockFunction")).isNotNull();
    assertThat(ReflectionTestUtils.getField(nonEndpointProxy, "mockFunction")).isNull();
  }
}
