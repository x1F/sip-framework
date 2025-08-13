package one.x1f.sip.foundation.core.proxies;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

/**
 * Class in charge of toggling all beans under one.x1f.sip.foundation.core.proxies package, based on
 * sip.core.proxy.enabled value (true by default).
 */
@AutoConfiguration
@ComponentScan
@ConditionalOnProperty(value = "sip.core.proxy.enabled", havingValue = "true")
public class ProxyAutoConfig {}
