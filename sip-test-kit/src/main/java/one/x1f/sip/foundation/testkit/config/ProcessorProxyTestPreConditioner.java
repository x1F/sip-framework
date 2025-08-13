package one.x1f.sip.foundation.testkit.config;

import lombok.RequiredArgsConstructor;
import one.x1f.sip.foundation.core.proxies.ProcessorProxyRegistry;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/** Adds a mock function to all outgoing Processor Proxies */
@Configuration
@RequiredArgsConstructor
public class ProcessorProxyTestPreConditioner {
  private final ProcessorProxyRegistry proxyRegistry;

  /**
   * Loops through the proxy registry and sets up default NOOP mock function for all outgoing
   * (Endpoint) Processors
   */
  @EventListener(ApplicationReadyEvent.class)
  public void setDefaultMockOnAllEndpointProcessors() {
    proxyRegistry
        .getProxies()
        .forEach(
            (id, processorProxy) -> {
              if (processorProxy.isEndpointProcessor()) {
                processorProxy.mock(exchange -> exchange);
              }
            });
  }
}
