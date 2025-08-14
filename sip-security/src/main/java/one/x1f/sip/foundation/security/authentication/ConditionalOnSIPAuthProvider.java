package one.x1f.sip.foundation.security.authentication;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import one.x1f.sip.foundation.security.config.ConditionalOnSIPSecurityAuthenticationEnabled;
import org.springframework.context.annotation.Conditional;

/**
 * Condition which allows us to control which auth providers and validators should be loaded.
 *
 * @author thomas.stieglmaier
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@ConditionalOnSIPSecurityAuthenticationEnabled
@Conditional(SIPAuthProviderCondition.class)
public @interface ConditionalOnSIPAuthProvider {

  /**
   * Non-mandatory setting, if no validation type is given, then the condition is true, as soon as
   * the specified property value in the list items exist. If it is set, then the validation part of
   * the auth provider settings is also checked for the validation type
   *
   * @return the validation type
   */
  Class<?> validationClass() default Object.class;

  /**
   * The fully qualified classname of the authentication provider relevant for the class this
   * annotation is put onto
   *
   * @return the classname
   */
  Class<?> listItemValue();
}
