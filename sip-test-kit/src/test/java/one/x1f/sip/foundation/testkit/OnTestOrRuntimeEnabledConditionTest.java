package one.x1f.sip.foundation.testkit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;

class OnTestOrRuntimeEnabledConditionTest {

  @Test
  void When_getMatchOutcome_With_TestProfile_Then_OutcomeIsMatch() {
    OnTestOrRuntimeEnabledCondition subject = new OnTestOrRuntimeEnabledCondition();
    ConditionContext context = mock(ConditionContext.class);
    Environment environment = mock(Environment.class);
    when(context.getEnvironment()).thenReturn(environment);
    String[] profiles = {"test"};
    when(environment.getActiveProfiles()).thenReturn(profiles);
    ConditionOutcome target = subject.getMatchOutcome(context, null);

    assertThat(target.isMatch()).isTrue();
  }

  @Test
  void When_getMatchOutcome_With_RuntimeEnabledAndCloud_Then_OutcomeIsMatch() {
    OnTestOrRuntimeEnabledCondition subject = new OnTestOrRuntimeEnabledCondition();
    ConditionContext context = mock(ConditionContext.class);
    Environment environment = mock(Environment.class);
    when(context.getEnvironment()).thenReturn(environment);
    when(environment.getActiveProfiles()).thenReturn(new String[] {});
    when(environment.getProperty(anyString(), eq(Boolean.class), eq(false))).thenReturn(true);
    ConditionOutcome target = subject.getMatchOutcome(context, null);

    assertThat(target.isMatch()).isTrue();
  }

  @Test
  void When_getMatchOutcome_With_RuntimeDisabled_Then_OutcomeIsNotMatch() {
    OnTestOrRuntimeEnabledCondition subject = new OnTestOrRuntimeEnabledCondition();
    ConditionContext context = mock(ConditionContext.class);
    Environment environment = mock(Environment.class);
    when(context.getEnvironment()).thenReturn(environment);
    when(environment.getActiveProfiles()).thenReturn(new String[] {});
    when(environment.getProperty(anyString(), eq(Boolean.class), eq(false))).thenReturn(false);
    ConditionOutcome target = subject.getMatchOutcome(context, null);

    assertThat(target.isMatch()).isFalse();
  }
}
