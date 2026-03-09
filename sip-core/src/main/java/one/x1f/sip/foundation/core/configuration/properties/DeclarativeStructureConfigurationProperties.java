package one.x1f.sip.foundation.core.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("sip.core.declarativestructure")
public class DeclarativeStructureConfigurationProperties {
  /** Enable declarative structure */
  private boolean enabled = true;
}
