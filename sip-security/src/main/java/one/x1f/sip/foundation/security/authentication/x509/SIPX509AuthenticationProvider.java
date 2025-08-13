package one.x1f.sip.foundation.security.authentication.x509;

import jakarta.annotation.PostConstruct;
import one.x1f.sip.foundation.security.authentication.ConditionalOnSIPAuthProvider;
import one.x1f.sip.foundation.security.authentication.SIPAuthenticationProvider;
import one.x1f.sip.foundation.security.authentication.common.extractors.TokenExtractors;
import one.x1f.sip.foundation.security.authentication.common.validators.SIPTokenValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * X509 authentication provider, wiring together the {@link SIPX509TokenExtractor} and the
 * configured {@link SIPTokenValidator}
 *
 * @author thomas
 */
@ConditionalOnSIPAuthProvider(listItemValue = SIPX509AuthenticationProvider.class)
@Component
public class SIPX509AuthenticationProvider
    extends SIPAuthenticationProvider<SIPX509AuthenticationToken> {

  private final TokenExtractors tokenExtractors;

  /**
   * Autowired constructor for creating the x509 authentication provider
   *
   * @param tokenExtractors the object to which the x509 token extractor should be added
   * @param tokenValidator the configured token validator
   */
  @Autowired
  public SIPX509AuthenticationProvider(
      TokenExtractors tokenExtractors,
      SIPTokenValidator<SIPX509AuthenticationToken> tokenValidator) {
    super(SIPX509AuthenticationToken.class, tokenValidator);
    this.tokenExtractors = tokenExtractors;
  }

  @PostConstruct
  private void postConstruct() {
    tokenExtractors.addMapping(getClass(), new SIPX509TokenExtractor());
  }
}
