package one.x1f.sip.foundation.core.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("sip.core.metrics.external-endpoint-health-check")
public class HealthCheckConfigurationProperties {
  /** Enable external endpoint health check */
  private boolean enabled = true;
}
