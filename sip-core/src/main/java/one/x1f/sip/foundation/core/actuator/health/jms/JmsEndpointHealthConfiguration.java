package one.x1f.sip.foundation.core.actuator.health.jms;

import one.x1f.sip.foundation.core.actuator.health.EndpointHealthConfigurer;
import org.apache.camel.component.jms.JmsComponent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers a default health check for JMS Camel endpoints. */
@ConditionalOnClass(JmsComponent.class)
@Configuration
public class JmsEndpointHealthConfiguration {

  @Bean
  EndpointHealthConfigurer jmsConfigurer() {
    return registry -> registry.register("jms://**", JmsHealthIndicators::connectionManageable);
  }
}
