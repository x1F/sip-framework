package one.x1f.sip.foundation.core.trace;

import org.apache.camel.support.processor.DefaultExchangeFormatter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Implementation of ExchangeFormatter for Tracing Exchanges */
@Component
@ConfigurationProperties("sip.core.tracing.exchange-formatter")
public class SIPExchangeFormatter extends DefaultExchangeFormatter {}
