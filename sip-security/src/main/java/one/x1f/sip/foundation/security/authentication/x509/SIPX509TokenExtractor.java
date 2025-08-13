package one.x1f.sip.foundation.security.authentication.x509;

import jakarta.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.security.authentication.common.extractors.SIPTokenExtractor;
import org.springframework.security.authentication.BadCredentialsException;

/**
 * Token extractor for getting the distinguished name of the client certificate.
 *
 * @author thomas.stieglmaier
 */
@Slf4j
public class SIPX509TokenExtractor implements SIPTokenExtractor<SIPX509AuthenticationToken> {

  @Override
  public SIPX509AuthenticationToken extract(HttpServletRequest request) {
    X509Certificate clientCertificate = extractClientCertificate(request);

    if (clientCertificate == null) {
      throw new BadCredentialsException("No client certificate found in request");
    }

    return new SIPX509AuthenticationToken(
        clientCertificate.getSubjectX500Principal().toString(), false);
  }

  @Override
  public Class<SIPX509AuthenticationToken> getTokenType() {
    return SIPX509AuthenticationToken.class;
  }

  private static X509Certificate extractClientCertificate(HttpServletRequest request) {
    X509Certificate[] certs =
        (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");

    if (certs != null && certs.length > 0) {
      if (log.isTraceEnabled()) {
        log.trace("sip.security.x509.clientcertificate_{}", certs[0]);
      }

      return certs[0];
    }

    return null;
  }
}
