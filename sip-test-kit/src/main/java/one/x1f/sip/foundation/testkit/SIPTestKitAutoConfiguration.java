package one.x1f.sip.foundation.testkit;

import one.x1f.sip.foundation.core.util.YamlPropertSourceFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Import(TestKitConfig.class)
@AutoConfiguration
@Conditional(OnTestOrRuntimeEnabledCondition.class)
@PropertySource(
    value = "classpath:sip-testkit-default-config.yaml",
    factory = YamlPropertSourceFactory.class)
public class SIPTestKitAutoConfiguration {}
