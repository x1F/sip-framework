package one.x1f.sip.foundation.security.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Collections;
import one.x1f.sip.foundation.security.authentication.basic.SIPBasicAuthAuthenticationToken;
import org.junit.jupiter.api.Test;

class CompositeAuthenticationTokenTest {

  @Test
  void WHEN_ctor_WITH_NullAuthTokens_THEN_isAuthenticatedFalse() {
    // act
    CompositeAuthenticationToken subject = new CompositeAuthenticationToken(null);

    // assert
    assertThat(subject.isAuthenticated()).isFalse();
  }

  @Test
  void WHEN_ctor_WITH_validParams_THEN_correctValueslReturned() throws Exception {
    // arrange
    SIPBasicAuthAuthenticationToken token =
        new SIPBasicAuthAuthenticationToken("user", "pw", false);

    // act
    CompositeAuthenticationToken subject =
        new CompositeAuthenticationToken(Collections.singletonList(token));

    // assert
    assertThat(subject.getAuthorities()).isEmpty();
    assertThat(subject.getCredentials()).isNull();
    assertThat(subject.getDetails()).isNull();
    assertThat(subject.getName()).isNull();
    assertThat(subject.getPrincipal()).isNull();
    assertThat(subject.isAuthenticated()).isFalse();
    assertThat(subject.getAuthTokens()).containsExactly(token);
  }

  @Test
  void WHEN_withAuthenticated_WITH_true_THEN_sameObjectWithTrueAuthReturned() throws Exception {
    // arrange
    CompositeAuthenticationToken subject =
        new CompositeAuthenticationToken(Collections.emptyList());

    // act + assert
    assertThatExceptionOfType(UnsupportedOperationException.class)
        .isThrownBy(() -> subject.withAuthenticated(true));
  }
}
