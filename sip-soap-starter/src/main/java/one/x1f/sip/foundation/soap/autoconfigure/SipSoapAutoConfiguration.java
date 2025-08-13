package one.x1f.sip.foundation.soap.autoconfigure;

import one.x1f.sip.foundation.core.util.YamlPropertSourceFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
@ComponentScan("one.x1f.sip.foundation.soap")
@ConditionalOnProperty(value = "sip.core.declarativestructure.enabled", havingValue = "true")
@PropertySource(
    value = "classpath:sip-soap-default-config.yaml",
    factory = YamlPropertSourceFactory.class)
public class SipSoapAutoConfiguration {}
