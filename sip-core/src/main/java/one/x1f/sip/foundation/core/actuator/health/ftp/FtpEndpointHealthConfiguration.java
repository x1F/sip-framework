package one.x1f.sip.foundation.core.actuator.health.ftp;

import one.x1f.sip.foundation.core.actuator.health.EndpointHealthConfigurer;
import org.apache.camel.component.file.remote.FtpComponent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** HealthConfiguration for FtpEndpoint */
@ConditionalOnClass(FtpComponent.class)
@Configuration
public class FtpEndpointHealthConfiguration {

  @Bean
  EndpointHealthConfigurer ftpConfigurer() {
    return registry -> registry.register("*ftp*://**", FtpHealthIndicators::noopHealthIndicator);
  }
}
