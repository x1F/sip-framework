package de.ikor.sip.foundation.core.proxies;

import de.ikor.sip.foundation.core.proxies.extension.ProxyExtension;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.*;
import org.apache.camel.spi.InterceptStrategy;
import org.springframework.stereotype.Component;

/** Apache Camel Processor creation interception */
@Slf4j
@Component
@RequiredArgsConstructor
public class AddProxyInterceptStrategy implements InterceptStrategy {
  private final ProcessorProxyRegistry proxyRegistry;
  private final List<ProxyExtension> extensions;
  private final CamelContext camelContext;

  @PostConstruct
  private void setup() {
    camelContext.getCamelContextExtension().addInterceptStrategy(this);
  }

  @Override
  public Processor wrapProcessorInInterceptors(
      CamelContext context, NamedNode definition, Processor target, Processor nextTarget)
      throws Exception {
    String processorId = definition.getId();
    log.debug("sip.core.proxy.register.info_{}", processorId);

    ProcessorProxy processorProxy = new ProcessorProxy(definition, target, extensions);
    proxyRegistry.register(processorId, processorProxy);
    return processorProxy;
  }
}
