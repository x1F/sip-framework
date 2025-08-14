package one.x1f.sip.foundation.security.authentication.common.extractors;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;
import one.x1f.sip.foundation.security.authentication.SIPAuthenticationProvider;
import one.x1f.sip.foundation.security.config.ConditionalOnSIPSecurityAuthenticationEnabled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Spring component providing a map of all token extractors and the related authentication
 * providers.
 *
 * @author thomas.stieglmaier
 */
@ConditionalOnSIPSecurityAuthenticationEnabled
@Component
public class TokenExtractors {
  private final Map<Class<?>, SIPTokenExtractor<?>> providerToExtractorMapping =
      new ConcurrentHashMap<>();

  /**
   * Adds a mapping to the token extractors. This is usually called in a {@link PostConstruct} block
   * of {@link SIPAuthenticationProvider} implementations.
   *
   * @param providerClassName the class of the provider adding the token extractor
   * @param tokenExtraction the tokenextractor itself
   */
  public void addMapping(Class<?> providerClassName, SIPTokenExtractor<?> tokenExtraction) {
    if (providerToExtractorMapping.containsKey(providerClassName)) {
      throw SIPFrameworkException.init(
          "Token extractor mapping for this provider exists already: %s", providerClassName);
    }

    if (providerToExtractorMapping.values().stream()
        .map(Object::getClass)
        .anyMatch(tokenExtraction.getClass()::equals)) {
      throw SIPFrameworkException.init(
          "Token extractor mapping for this extractor exists already: %s",
          tokenExtraction.getClass());
    }

    if (providerToExtractorMapping.values().stream()
        .map(SIPTokenExtractor::getTokenType)
        .anyMatch(tokenExtraction.getTokenType()::equals)) {
      throw SIPFrameworkException.init(
          "A token extractor mapping for an extractor with the same token type already exists: %s",
          tokenExtraction.getTokenType());
    }

    providerToExtractorMapping.put(providerClassName, tokenExtraction);
  }

  /**
   * Extracts the token for a given auth provider and request.
   *
   * @param authProvider the auth provider for which the token should be extracted
   * @param request the request from which the token should be extracted
   * @return the extracted token
   */
  public Authentication extractTokenFor(Class<?> authProvider, HttpServletRequest request) {
    return providerToExtractorMapping.get(authProvider).extract(request);
  }
}
