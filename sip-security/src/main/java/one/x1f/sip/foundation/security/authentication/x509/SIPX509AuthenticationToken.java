package one.x1f.sip.foundation.security.authentication.x509;

import lombok.Getter;
import lombok.NonNull;
import one.x1f.sip.foundation.security.authentication.SIPAuthenticationToken;

/**
 * Container for the certificates dn data, containing all relevant information for validation later
 * on
 *
 * @author thomas.stieglmaier
 */
@Getter
public class SIPX509AuthenticationToken extends SIPAuthenticationToken<SIPX509AuthenticationToken> {

  private final String principal;

  /**
   * Creates a x509 token
   *
   * @param principal the certificate's data as a string
   * @param authenticated indicates of the token is authenticated or not
   */
  public SIPX509AuthenticationToken(@NonNull String principal, boolean authenticated) {
    super(authenticated);
    this.principal = principal;
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }

  @Override
  public SIPX509AuthenticationToken withAuthenticated(boolean authenticated) {
    return new SIPX509AuthenticationToken(principal, authenticated);
  }
}
