package one.x1f.sip.foundation.security.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import one.x1f.sip.foundation.security.authentication.basic.SIPBasicAuthAuthenticationProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

class SIPAuthProvidersExistConditionTest {

  private SIPAuthProvidersExistCondition subject = new SIPAuthProvidersExistCondition();

  @Test
  void When_getMatchOutcomeWithExistingFlag_Expect_match() throws Exception {
    // arrange
    Class<?> annotatedClass = SIPBasicAuthAuthenticationProvider.class;

    ConditionContext context = mock(ConditionContext.class);
    MockEnvironment environment = new MockEnvironment();
    environment.setProperty(
        "sip.security.authentication.auth-providers[0].classname", annotatedClass.getName());

    when(context.getEnvironment()).thenReturn(environment);
    AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

    // act
    ConditionOutcome result = subject.getMatchOutcome(context, metadata);

    // assert
    assertThat(result.isMatch()).isTrue();
  }

  @Test
  void When_getMatchOutcomeWithNonExistingFlag_Expect_noMatch() throws Exception {
    // arrange
    ConditionContext context = mock(ConditionContext.class);
    MockEnvironment environment = new MockEnvironment();

    when(context.getEnvironment()).thenReturn(environment);
    AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

    // act
    ConditionOutcome result = subject.getMatchOutcome(context, metadata);

    // assert
    assertThat(result.isMatch()).isFalse();
  }
}
