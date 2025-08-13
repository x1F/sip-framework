package one.x1f.sip.foundation.security.authentication.basic;

import jakarta.annotation.PostConstruct;
import one.x1f.sip.foundation.security.authentication.ConditionalOnSIPAuthProvider;
import one.x1f.sip.foundation.security.authentication.SIPAuthenticationProvider;
import one.x1f.sip.foundation.security.authentication.common.extractors.TokenExtractors;
import one.x1f.sip.foundation.security.authentication.common.validators.SIPTokenValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Basic Authentication provider, wiring together the {@link SIPBasicAuthTokenExtractor} and the
 * configured {@link SIPTokenValidator}
 *
 * @author thomas.stieglmaier
 */
@ConditionalOnSIPAuthProvider(listItemValue = SIPBasicAuthAuthenticationProvider.class)
@Component
public class SIPBasicAuthAuthenticationProvider
    extends SIPAuthenticationProvider<SIPBasicAuthAuthenticationToken> {

  private TokenExtractors tokenExtractors;

  /**
   * Autowired constructor for creating the basic authentication provider
   *
   * @param tokenExtractors the object to which the basic auth token extractor should be added
   * @param tokenValidator the configured token validator
   */
  @Autowired
  public SIPBasicAuthAuthenticationProvider(
      TokenExtractors tokenExtractors,
      SIPTokenValidator<SIPBasicAuthAuthenticationToken> tokenValidator) {
    super(SIPBasicAuthAuthenticationToken.class, tokenValidator);
    this.tokenExtractors = tokenExtractors;
  }

  @PostConstruct
  private void postConstruct() {
    tokenExtractors.addMapping(getClass(), new SIPBasicAuthTokenExtractor());
  }
}
