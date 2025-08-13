package one.x1f.sip.foundation.security;

import one.x1f.sip.foundation.core.util.YamlPropertSourceFactory;
import one.x1f.sip.foundation.security.config.SecurityConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

/**
 * Spring-boot autoconfiguration entrypoint (referenced by the <code>
 * src/main/resource/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
 * </code> file.
 *
 * @author thomas.stieglmaier
 */
@ConditionalOnClass(SecurityConfig.class)
@ComponentScan("one.x1f.sip.foundation.security")
@PropertySource(
    value = "classpath:sip-security-default-config.yaml",
    factory = YamlPropertSourceFactory.class)
public class SIPSecurityAutoConfiguration {}
