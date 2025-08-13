package one.x1f.sip.foundation.core.actuator.health;

import java.util.List;
import org.apache.camel.CamelContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Main configuration class that instantiates and configures {@link EndpointHealthRegistry } along
 * with {@link CamelEndpointHealthMonitor}.
 */
@Configuration
public class CamelEndpointHealthConfiguration {

  @Bean
  EndpointHealthRegistry endpointHealthRegistry(List<EndpointHealthConfigurer> configurers) {
    EndpointHealthRegistry registry = new EndpointHealthRegistry();
    configurers.forEach(configurer -> configurer.configure(registry));
    return registry;
  }

  @Bean("sipEndpointHealthMonitor")
  CamelEndpointHealthMonitor camelEndpointHealthMonitor(
      CamelContext ctx, EndpointHealthRegistry registry) {
    return new CamelEndpointHealthMonitor(ctx, registry);
  }
}
