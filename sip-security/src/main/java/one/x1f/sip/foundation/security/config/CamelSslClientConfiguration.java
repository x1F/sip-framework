package one.x1f.sip.foundation.security.config;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.apache.camel.CamelContext;
import org.apache.camel.component.http.HttpComponent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Configure camel security related settings
 *
 * @author thomas.stieglmaier
 */
@Configuration
@ConditionalOnProperty(name = "sip.security.ssl.client.enabled", havingValue = "true")
@AllArgsConstructor
public class CamelSslClientConfiguration {

  private final CamelContext camelContext;

  /**
   * Configure the httpcomponent of camel rest client to use custom ssl settings (different
   * keystores, potentially client certificates) if necessary
   */
  @PostConstruct
  public void initCamelContextClientSsl() {
    HttpComponent httpComponent = camelContext.getComponent("https", HttpComponent.class);
    httpComponent.setSslContextParameters(camelContext.getSSLContextParameters());
  }
}
