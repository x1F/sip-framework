package one.x1f.sip.foundation.core.trace;

import one.x1f.sip.foundation.core.annotation.SIPFeature;
import one.x1f.sip.foundation.core.util.FoundationFeature;
import org.apache.camel.CamelContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

/**
 * Class in charge of toggling all beans under one.x1f.sip.foundation.core.trace package, based on
 * sip.core.tracing.enabled value (false by default).
 */
@SIPFeature(name = FoundationFeature.TRACING, versions = 1)
@ComponentScan
@AutoConfiguration
@ConditionalOnProperty(value = "sip.core.tracing.enabled", havingValue = "true")
public class TraceAutoConfig {
  public TraceAutoConfig(CamelContext camelContext) {
    camelContext.setTracing(true);
  }
}
