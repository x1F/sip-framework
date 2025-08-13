package one.x1f.sip.foundation.security.authentication.common.validators;

import static org.assertj.core.api.Assertions.assertThat;

import one.x1f.sip.foundation.security.authentication.basic.SIPBasicAuthAuthenticationToken;
import org.junit.jupiter.api.Test;

class SIPAlwaysAllowValidatorTest {

  @Test
  void WHEN_isValid_WITH_anyToken_THEN_trueReturned() throws Exception {
    // arrange
    SIPAlwaysAllowValidator<SIPBasicAuthAuthenticationToken> subject =
        new SIPAlwaysAllowValidator<>();

    // act
    boolean result = subject.isValid(null);

    // assert
    assertThat(result).isTrue();
  }
}
