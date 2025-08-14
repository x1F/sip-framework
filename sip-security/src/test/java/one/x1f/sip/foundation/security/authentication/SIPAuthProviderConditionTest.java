package one.x1f.sip.foundation.security.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import one.x1f.sip.foundation.security.authentication.basic.SIPBasicAuthAuthenticationProvider;
import one.x1f.sip.foundation.security.authentication.basic.SIPBasicAuthFileValidator;
import one.x1f.sip.foundation.security.authentication.common.validators.SIPAlwaysAllowValidator;
import one.x1f.sip.foundation.security.authentication.x509.SIPX509AuthenticationProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

class SIPAuthProviderConditionTest {

  private static final String AUTH_PROVIDERS_PROPERTY_CLASSNAME =
      "sip.security.authentication.auth-providers[0].classname";
  private static final String AUTH_PROVIDERS_VALIDATION_CLASSNAME =
      "sip.security.authentication.auth-providers[0].validation.classname";
  private SIPAuthProviderCondition subject = new SIPAuthProviderCondition();

  @Test
  void WHEN_getMatchOutcome_WITH_validCondition_THEN_match() {
    // arrange
    Class<?> annotatedClass = SIPBasicAuthAuthenticationProvider.class;

    ConditionContext context = mock(ConditionContext.class);
    MockEnvironment environment = new MockEnvironment();
    environment.setProperty(AUTH_PROVIDERS_PROPERTY_CLASSNAME, annotatedClass.getName());

    when(context.getEnvironment()).thenReturn(environment);
    AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

    MultiValueMap<String, Object> annotations = new LinkedMultiValueMap<>();
    annotations.add("listItemValue", annotatedClass);
    annotations.add("validationClass", Object.class);

    when(metadata.getAllAnnotationAttributes(ConditionalOnSIPAuthProvider.class.getName()))
        .thenReturn(annotations);

    // act
    ConditionOutcome result = subject.getMatchOutcome(context, metadata);

    // assert
    assertThat(result.isMatch()).isTrue();
  }

  @Test
  void WHEN_getMatchOutcome_WITH_invalidCondition_THEN_noMatch() {
    // arrange
    Class<?> annotatedClass = SIPBasicAuthAuthenticationProvider.class;
    Class<?> configuredClass = SIPX509AuthenticationProvider.class;

    ConditionContext context = mock(ConditionContext.class);
    MockEnvironment environment = new MockEnvironment();
    environment.setProperty(AUTH_PROVIDERS_PROPERTY_CLASSNAME, configuredClass.getName());

    when(context.getEnvironment()).thenReturn(environment);
    AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

    MultiValueMap<String, Object> annotations = new LinkedMultiValueMap<>();
    annotations.add("listItemValue", annotatedClass);
    annotations.add("validationClass", Object.class);

    when(metadata.getAllAnnotationAttributes(ConditionalOnSIPAuthProvider.class.getName()))
        .thenReturn(annotations);

    // act
    ConditionOutcome result = subject.getMatchOutcome(context, metadata);

    // assert
    assertThat(result.isMatch()).isFalse();
  }

  @Test
  void WHEN_getMatchOutcome_WITH_invalidConditionWithValidationClass_THEN_noMatch() {
    // arrange
    Class<?> annotatedClass = SIPBasicAuthAuthenticationProvider.class;

    ConditionContext context = mock(ConditionContext.class);
    MockEnvironment environment = new MockEnvironment();
    environment.setProperty(AUTH_PROVIDERS_PROPERTY_CLASSNAME, annotatedClass.getName());
    environment.setProperty(
        AUTH_PROVIDERS_VALIDATION_CLASSNAME, SIPAlwaysAllowValidator.class.getName());

    when(context.getEnvironment()).thenReturn(environment);
    AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

    MultiValueMap<String, Object> annotations = new LinkedMultiValueMap<>();
    annotations.add("listItemValue", annotatedClass);
    annotations.add("validationClass", SIPBasicAuthFileValidator.class);

    when(metadata.getAllAnnotationAttributes(ConditionalOnSIPAuthProvider.class.getName()))
        .thenReturn(annotations);

    // act
    ConditionOutcome result = subject.getMatchOutcome(context, metadata);

    // assert
    assertThat(result.isMatch()).isFalse();
  }

  @Test
  void WHEN_getMatchOutcome_WITH_validConditionWithValidationClass_THEN_match() {
    // arrange
    Class<?> annotatedClass = SIPBasicAuthAuthenticationProvider.class;
    Class<?> validatorClass = SIPBasicAuthFileValidator.class;

    ConditionContext context = mock(ConditionContext.class);
    MockEnvironment environment = new MockEnvironment();
    environment.setProperty(AUTH_PROVIDERS_PROPERTY_CLASSNAME, annotatedClass.getName());
    environment.setProperty(AUTH_PROVIDERS_VALIDATION_CLASSNAME, validatorClass.getName());

    when(context.getEnvironment()).thenReturn(environment);
    AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

    MultiValueMap<String, Object> annotations = new LinkedMultiValueMap<>();
    annotations.add("listItemValue", annotatedClass);
    annotations.add("validationClass", validatorClass);

    when(metadata.getAllAnnotationAttributes(ConditionalOnSIPAuthProvider.class.getName()))
        .thenReturn(annotations);

    // act
    ConditionOutcome result = subject.getMatchOutcome(context, metadata);

    // assert
    assertThat(result.isMatch()).isTrue();
  }

  @Test
  void WHEN_getMatchOutcome_WITH_noConfig_THEN_noMatch() {
    // arrange
    ConditionContext context = mock(ConditionContext.class);

    when(context.getEnvironment()).thenReturn(new MockEnvironment());
    AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

    MultiValueMap<String, Object> annotations = new LinkedMultiValueMap<>();
    annotations.add("listItemValue", SIPBasicAuthAuthenticationProvider.class);
    annotations.add("validationClass", SIPBasicAuthFileValidator.class);

    when(metadata.getAllAnnotationAttributes(ConditionalOnSIPAuthProvider.class.getName()))
        .thenReturn(annotations);

    // act
    ConditionOutcome result = subject.getMatchOutcome(context, metadata);

    // assert
    assertThat(result.isMatch()).isFalse();
  }
}
