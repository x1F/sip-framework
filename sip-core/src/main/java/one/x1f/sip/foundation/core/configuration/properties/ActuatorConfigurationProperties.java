package one.x1f.sip.foundation.core.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("actuator.adapter-routes")
public class ActuatorConfigurationProperties {
  /** Enable controlling lifecycle of routes */
  private boolean enabled = true;

  private ActuatorSchedulerConfigurationProperties scheduler;

  @Data
  public static class ActuatorSchedulerConfigurationProperties {
    /** Sets health check execution interval */
    private String fixedDelay;

    /** Sets health check initial delay */
    private String initialDelay;
  }
}
