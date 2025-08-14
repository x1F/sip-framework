package one.x1f.sip.foundation.core.proxies;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.springframework.context.annotation.Configuration;

/** Configuration for ProcessorProxy */
@Slf4j
@Configuration
@AllArgsConstructor
public class ProcessorProxyConfiguration {
  private final CamelContext camelContext;
  private final ProcessorProxyRegistry proxyRegistry;

  /**
   * Auto executed method for registering {@link ProcessorProxyRegistry} in {@link CamelContext} as
   * extension
   */
  @PostConstruct
  public void addProxyRegistryToCamelContext() {
    log.info("Configuring dynamic processors as extension in the camel context");
    camelContext
        .getCamelContextExtension()
        .addContextPlugin(ProcessorProxyRegistry.class, proxyRegistry);
  }
}
