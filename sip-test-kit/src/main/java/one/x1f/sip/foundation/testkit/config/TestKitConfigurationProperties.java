package one.x1f.sip.foundation.testkit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("sip.testkit")
public class TestKitConfigurationProperties {
  /** Enable Test Kit */
  private boolean enabled;

  /** Enable batch tests in Test Kit */
  private boolean batchTest;

  /** Define path for file with test cases */
  private String testCasesPath;
}
