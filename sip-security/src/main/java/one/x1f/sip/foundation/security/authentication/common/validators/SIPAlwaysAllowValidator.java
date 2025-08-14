package one.x1f.sip.foundation.security.authentication.common.validators;

import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.security.authentication.SIPAuthenticationToken;
import one.x1f.sip.foundation.security.config.ConditionalOnSIPSecurityAuthenticationEnabled;
import org.springframework.stereotype.Component;

/**
 * Dummy validator which does no checks, and allows each token to be authenticated.
 *
 * @author thomas.stieglmaier
 * @param <T> the token type to be validated
 */
@Slf4j
@Component
@ConditionalOnSIPSecurityAuthenticationEnabled
public class SIPAlwaysAllowValidator<T extends SIPAuthenticationToken<T>>
    implements SIPTokenValidator<T> {

  @Override
  public boolean isValid(T token) {
    log.warn("sip.security.alwaysvalid");
    return true;
  }
}
