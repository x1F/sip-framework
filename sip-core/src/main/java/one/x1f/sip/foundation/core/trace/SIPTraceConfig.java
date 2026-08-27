package one.x1f.sip.foundation.core.trace;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Configuration class to read from property file */
@Getter
@Setter
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "sip.core.tracing")
public class SIPTraceConfig {

  private boolean log;
  private SIPTraceConfigLogging logging;

  @Getter
  @Setter
  public static class SIPTraceConfigLogging {
    private boolean basic;
    private boolean connector;
    private boolean connectorExtension;
  }
}
