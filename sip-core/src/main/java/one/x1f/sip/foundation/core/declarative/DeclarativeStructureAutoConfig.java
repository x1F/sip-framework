package one.x1f.sip.foundation.core.declarative;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan
@ConditionalOnProperty(value = "sip.core.declarativestructure.enabled", havingValue = "true")
public class DeclarativeStructureAutoConfig {}
