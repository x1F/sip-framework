package one.x1f.sip.foundation.security.authentication;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.Collection;
import java.util.Optional;
import one.x1f.sip.foundation.security.config.SecurityConfigProperties.AuthProviderSettings;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.MultiValueMap;

/**
 * Matcher class implementing the logic for the {@link ConditionalOnSIPAuthProvider} annotation.
 * Look there for details on the possible configuration.
 *
 * @author thomas.stieglmaier
 */
public class SIPAuthProviderCondition extends SpringBootCondition {

  @Override
  public ConditionOutcome getMatchOutcome(
      ConditionContext context, AnnotatedTypeMetadata metadata) {
    MultiValueMap<String, Object> attributes =
        metadata.getAllAnnotationAttributes(ConditionalOnSIPAuthProvider.class.getName());

    Collection<AuthProviderSettings> authSettings =
        AuthProviderSettings.bindFromPropertySource(context.getEnvironment());

    if (isNotEmpty(authSettings) && attributes != null) {
      Class<?> listItemValue = (Class<?>) attributes.get("listItemValue").get(0);
      Class<?> validationClass = (Class<?>) attributes.get("validationClass").get(0);

      Optional<AuthProviderSettings> authProviderOpt =
          authSettings.stream().filter(s -> s.getClassname().equals(listItemValue)).findFirst();
      if (authProviderOpt.isPresent()) {
        AuthProviderSettings authProvider = authProviderOpt.get();

        if (!validationClass.equals(Object.class)
            && !authProvider.getValidation().getClassname().equals(validationClass)) {
          return ConditionOutcome.noMatch(
              ConditionMessage.forCondition("SIP validation type check")
                  .didNotFind("property")
                  .items(AuthProviderSettings.AUTH_PROVIDERS_PROPERTY_NAME));
        }
        return ConditionOutcome.match();
      }
    }

    return ConditionOutcome.noMatch(
        ConditionMessage.forCondition("SIP list location")
            .didNotFind("property")
            .items(AuthProviderSettings.AUTH_PROVIDERS_PROPERTY_NAME));
  }
}
