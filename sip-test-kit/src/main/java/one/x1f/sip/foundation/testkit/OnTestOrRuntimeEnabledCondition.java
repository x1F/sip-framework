package one.x1f.sip.foundation.testkit;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;

public class OnTestOrRuntimeEnabledCondition extends SpringBootCondition {
  private static final String CLOUD_TESTKIT_AUTOCONFIGURATION_CLASS =
      "one.x1f.sip.cloud.testkit.SIPCloudTestKitAutoConfiguration";

  @Override
  public ConditionOutcome getMatchOutcome(
      ConditionContext context, AnnotatedTypeMetadata metadata) {
    List<String> activeProfiles = List.of(context.getEnvironment().getActiveProfiles());
    if (activeProfiles.contains("test")) {
      return ConditionOutcome.match("Active profile is 'test'");
    }

    boolean isEnabled =
        context.getEnvironment().getProperty("sip.testkit.enabled", Boolean.class, false);
    if (ClassUtils.isPresent(CLOUD_TESTKIT_AUTOCONFIGURATION_CLASS, context.getClassLoader())
        && isEnabled) {

      return ConditionOutcome.match("Runtime tests are enabled");
    }

    return ConditionOutcome.noMatch("Neither 'test' profile is found nor runtime test is enabled ");
  }
}
