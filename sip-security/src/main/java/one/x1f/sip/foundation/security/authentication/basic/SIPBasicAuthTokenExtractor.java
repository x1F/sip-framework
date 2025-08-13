package one.x1f.sip.foundation.security.authentication.basic;

import jakarta.servlet.http.HttpServletRequest;
import one.x1f.sip.foundation.security.authentication.common.extractors.SIPTokenExtractor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.www.BasicAuthenticationConverter;

/**
 * Token extractor for getting username and password from the Basic Authentication header.
 *
 * @author thomas.stieglmaier
 */
public class SIPBasicAuthTokenExtractor
    implements SIPTokenExtractor<SIPBasicAuthAuthenticationToken> {

  private final BasicAuthenticationConverter baConverter = new BasicAuthenticationConverter();

  @Override
  public SIPBasicAuthAuthenticationToken extract(HttpServletRequest request) {
    UsernamePasswordAuthenticationToken token = baConverter.convert(request);

    if (token == null) {
      throw new BadCredentialsException("No valid basic auth header found in request");
    }

    return new SIPBasicAuthAuthenticationToken(
        token.getPrincipal().toString(), token.getCredentials().toString(), false);
  }

  @Override
  public Class<SIPBasicAuthAuthenticationToken> getTokenType() {
    return SIPBasicAuthAuthenticationToken.class;
  }
}
