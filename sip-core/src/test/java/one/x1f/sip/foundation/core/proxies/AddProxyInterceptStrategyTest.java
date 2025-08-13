package one.x1f.sip.foundation.core.proxies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import one.x1f.sip.foundation.core.proxies.extension.ProxyExtension;
import org.apache.camel.CamelContext;
import org.apache.camel.NamedNode;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddProxyInterceptStrategyTest {
  private static final String PROCESSOR_ID = "processorId";
  private AddProxyInterceptStrategy addProxyInterceptStrategy;
  private final ProcessorProxyRegistry proxyRegistry = new ProcessorProxyRegistry();
  private final List<ProxyExtension> extensions = new ArrayList<>();
  @Mock private NamedNode definition;
  @Mock private Processor original;
  private final CamelContext camelContext = new DefaultCamelContext();

  @BeforeEach
  void setup() {
    addProxyInterceptStrategy =
        new AddProxyInterceptStrategy(proxyRegistry, extensions, camelContext);
    when(definition.getId()).thenReturn(PROCESSOR_ID);
  }

  @Test
  void WHEN_wrapProcessorInInterceptors_WITH_validParams_THEN_wrapped() throws Exception {
    // arrange

    // act
    addProxyInterceptStrategy.wrapProcessorInInterceptors(null, definition, null, original);

    // assert
    Optional<ProcessorProxy> proxy = proxyRegistry.getProxy(PROCESSOR_ID);
    assertThat(proxy).isPresent();
    assertThat(proxyRegistry.getProxies()).hasSize(1);
  }
}
