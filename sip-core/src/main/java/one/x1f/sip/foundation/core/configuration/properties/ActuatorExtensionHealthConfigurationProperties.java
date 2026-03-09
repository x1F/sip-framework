package one.x1f.sip.foundation.core.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("sip.core.actuator.extensions.health")
public class ActuatorExtensionHealthConfigurationProperties {
  /** Enable additional SIP Health check */
  private boolean enabled = true;
}
