package one.x1f.sip.foundation.security.authentication;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import one.x1f.sip.foundation.security.config.SecurityConfigProperties.AuthProviderSettings;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/** Condition class matching whether authentication providers are defined */
public class SIPAuthProvidersExistCondition extends SpringBootCondition {

  @Override
  public ConditionOutcome getMatchOutcome(
      ConditionContext context, AnnotatedTypeMetadata metadata) {
    ConditionOutcome outcome = ConditionOutcome.match();

    if (isEmpty(AuthProviderSettings.bindFromPropertySource(context.getEnvironment()))) {
      outcome =
          ConditionOutcome.noMatch(
              ConditionMessage.forCondition("SIP auth providers condition")
                  .didNotFind("property")
                  .items(AuthProviderSettings.AUTH_PROVIDERS_PROPERTY_NAME));
    }
    return outcome;
  }
}
