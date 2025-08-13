package one.x1f.sip.foundation.core.translate;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

/**
 * Class in charge of toggling all beans under one.x1f.sip.foundation.core.translate package, based
 * on sip.core.translation.enabled value (true by default).
 */
@AutoConfiguration
@ComponentScan
@ConditionalOnProperty(value = "sip.core.translation.enabled", havingValue = "true")
public class TranslationsAutoConfig {}
