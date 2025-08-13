package one.x1f.sip.foundation.core.actuator.health.http;

import one.x1f.sip.foundation.core.actuator.health.EndpointHealthConfigurer;
import org.apache.camel.Endpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link HttpEndpointHealthConfiguration} does the default autoconfiguration of the Camel's HTTP
 * and HTTPS endpoints.
 */
@Configuration
public class HttpEndpointHealthConfiguration {

  /**
   * Registers {@link HttpHealthIndicators#alwaysUnknown(Endpoint)} as a default health-checking
   * function for HTTP and HTTPS endpoints but just for listing purposes. Detected endpoints are
   * marked as unknown to indicate that they are present.
   */
  @Bean
  EndpointHealthConfigurer defaultHttpConfigurer() {
    return registry -> registry.register("http*://**", HttpHealthIndicators::alwaysUnknown);
  }
}
