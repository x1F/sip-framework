package one.x1f.sip.foundation.security.authentication.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;

class SIPBasicAuthTokenExtractorTest {

  private final SIPBasicAuthTokenExtractor subject = new SIPBasicAuthTokenExtractor();

  @Test
  void WHEN_extract_WITH_validAuthHeader_THEN_headerExtracted() throws Exception {
    // arrange
    String expectedUsername = "user";
    String expectedPassword = "password";

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Basic dXNlcjpwYXNzd29yZA==");

    // act
    SIPBasicAuthAuthenticationToken result = subject.extract(request);

    // assert
    assertThat(result.getPrincipal()).isEqualTo(expectedUsername);
    assertThat(result.getCredentials()).isEqualTo(expectedPassword);
  }

  @Test
  void WHEN_extract_WITH_badAuthHeader_THEN_badCredentialsException() throws Exception {
    // arrange
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "bad");

    // act + assert
    assertThatExceptionOfType(BadCredentialsException.class)
        .isThrownBy(() -> subject.extract(request));
  }

  @Test
  void WHEN_extract_WITH_noHeader_THEN_badCredentialsException() throws Exception {
    // arrange
    MockHttpServletRequest request = new MockHttpServletRequest();

    // act + assert
    assertThatExceptionOfType(BadCredentialsException.class)
        .isThrownBy(() -> subject.extract(request));
  }

  @Test
  void WHEN_getTokenType_THEN_returnBasicAuthype() throws Exception {
    assertThat(subject.getTokenType()).isEqualTo(SIPBasicAuthAuthenticationToken.class);
  }
}
