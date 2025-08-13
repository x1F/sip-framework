package one.x1f.sip.foundation.core.actuator.routes;

import one.x1f.sip.foundation.core.annotation.SIPFeature;
import one.x1f.sip.foundation.core.util.FoundationFeature;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

/** In charge of scanning all beans under one.x1f.sip.foundation.core.actuator.routes package. */
@SIPFeature(name = FoundationFeature.ADAPTER_ROUTES, versions = 2)
@AutoConfiguration
@ComponentScan
@ConditionalOnProperty(value = "sip.core.actuator.adapter-routes.enabled", havingValue = "true")
public class ActuatorRoutesAutoConfig {}
