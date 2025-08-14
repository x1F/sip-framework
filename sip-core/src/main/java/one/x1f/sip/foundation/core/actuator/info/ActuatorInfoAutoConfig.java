package one.x1f.sip.foundation.core.actuator.info;

import one.x1f.sip.foundation.core.annotation.SIPFeature;
import one.x1f.sip.foundation.core.util.FoundationFeature;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

/**
 * Class in charge of toggling all beans under one.x1f.sip.foundation.core.actuator.info package,
 * based on sip.core.actuator.extensions.info.enabled value (true by default).
 */
@SIPFeature(name = FoundationFeature.INFO, versions = 1)
@ComponentScan
@AutoConfiguration
@ConditionalOnAvailableEndpoint(endpoint = InfoEndpoint.class)
@ConditionalOnProperty(value = "sip.core.actuator.extensions.info.enabled", havingValue = "true")
public class ActuatorInfoAutoConfig {}
