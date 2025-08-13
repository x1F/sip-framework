package one.x1f.sip.foundation.core;

import one.x1f.sip.foundation.core.util.YamlPropertSourceFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.PropertySource;

/** Configuration class for including sip-core-default-config.yaml configuration into classpath */
@AutoConfiguration
@PropertySource(
    value = "classpath:sip-core-default-config.yaml",
    factory = YamlPropertSourceFactory.class)
public class SIPCoreAutoConfiguration {}
