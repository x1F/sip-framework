package one.x1f.sip.foundation.security.authentication.basic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import one.x1f.sip.foundation.security.authentication.common.extractors.TokenExtractors;
import one.x1f.sip.foundation.security.authentication.common.validators.SIPAlwaysAllowValidator;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SIPBasicAuthAuthenticationProviderTest {

  @Test
  void WHEN_postConstruct_THEN_tokenExtractorAdded() {
    // arrange
    TokenExtractors extractors = mock(TokenExtractors.class);
    SIPBasicAuthAuthenticationProvider subject =
        new SIPBasicAuthAuthenticationProvider(extractors, new SIPAlwaysAllowValidator<>());

    // act
    ReflectionTestUtils.invokeMethod(subject, "postConstruct");

    // assert
    verify(extractors)
        .addMapping(
            eq(SIPBasicAuthAuthenticationProvider.class), any(SIPBasicAuthTokenExtractor.class));
  }
}
