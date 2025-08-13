package one.x1f.sip.foundation.core.configuration;

import one.x1f.sip.foundation.core.util.ExtendedEventFactory;
import org.apache.camel.CamelContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;

@AutoConfiguration
public class CamelContextConfiguration {
  public CamelContextConfiguration(CamelContext camelContext) {
    camelContext.getManagementStrategy().setEventFactory(new ExtendedEventFactory());
  }
}
