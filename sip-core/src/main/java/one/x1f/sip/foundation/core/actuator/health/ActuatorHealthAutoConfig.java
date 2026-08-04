package one.x1f.sip.foundation.core.actuator.health;

import one.x1f.sip.foundation.core.annotation.SIPFeature;
import one.x1f.sip.foundation.core.util.FoundationFeature;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.context.annotation.ComponentScan;

/**
 * Class in charge of toggling all beans under one.x1f.sip.foundation.core.actuator.health package,
 * based on sip.core.actuator.extensions.health.enabled value (true by default).
 */
@SIPFeature(name = FoundationFeature.HEALTH, versions = 1)
@AutoConfiguration
@ComponentScan
@ConditionalOnAvailableEndpoint(endpoint = HealthEndpoint.class)
@ConditionalOnProperty(value = "sip.core.actuator.extensions.health.enabled", havingValue = "true")
public class ActuatorHealthAutoConfig {}
