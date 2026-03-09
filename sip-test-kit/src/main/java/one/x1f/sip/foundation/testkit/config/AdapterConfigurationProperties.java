package one.x1f.sip.foundation.testkit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("sip.adapter")
public class AdapterConfigurationProperties {
  private String camelEndpointContextPath;
  private String camelCxfEndpointContextPath;
}
