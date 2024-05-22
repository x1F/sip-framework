package de.ikor.sip.foundation.core.declarative.utils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import de.ikor.sip.foundation.core.declarative.RouteRole;
import de.ikor.sip.foundation.core.declarative.connector.ConnectorType;
import de.ikor.sip.foundation.core.declarative.utils.DeclarativeHelperTestModels.ExceptionThrowingConstructorMapper;
import de.ikor.sip.foundation.core.declarative.utils.DeclarativeHelperTestModels.MultipleMethodsMapper;
import de.ikor.sip.foundation.core.declarative.utils.DeclarativeHelperTestModels.MyExtendedIntegerList;
import de.ikor.sip.foundation.core.declarative.utils.DeclarativeHelperTestModels.NoArgsConstructorMapper;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Random;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DeclarativeHelperTest {

  @Test
  void WHEN_annotationNotPresent_THEN_throwSIPFrameworkException() {
    assertThatThrownBy(
            () -> DeclarativeReflectionUtils.getAnnotationOrThrow(Test.class, new Random()))
        .isInstanceOf(SIPFrameworkInitializationException.class)
        .hasMessage(
            "Annotation @%s required on class %s", Test.class.getSimpleName(), Random.class);
  }

  @Test
  void WHEN_createMapperWithoutNoArgConstructor_THEN_throwSIPException() {

    assertThatThrownBy(() -> DeclarativeHelper.createMapperInstance(NoArgsConstructorMapper.class))
        .isInstanceOf(SIPFrameworkInitializationException.class)
        .hasMessage(
            "Mapper %s needs to have a no-arg constructor, please define one.",
            NoArgsConstructorMapper.class.getName());
  }

  @Test
  void WHEN_createMapperWhichThrowsError_THEN_throwSIPExceptionWhichWrapsRuntimeException() {

    assertThatThrownBy(
            () -> DeclarativeHelper.createMapperInstance(ExceptionThrowingConstructorMapper.class))
        .isInstanceOf(SIPFrameworkInitializationException.class)
        .hasMessage(
            "SIP couldn't create a Mapper %s.", ExceptionThrowingConstructorMapper.class.getName())
        .hasCauseInstanceOf(InvocationTargetException.class)
        .hasStackTraceContaining("Exception message");
  }

  @Test
  void WHEN_mapperHasMoreThanOneMappingMethod_THEN_throwSIPException() {
    assertThatThrownBy(() -> DeclarativeHelper.getMappingMethod(MultipleMethodsMapper.class))
        .isInstanceOf(SIPFrameworkInitializationException.class)
        .hasMessage(
            "Failed to automatically resolve the model classes for the Mapper: %s. Please @Override the getSourceModelClass() and getTargetModelClass() methods",
            MultipleMethodsMapper.class.getName());
  }

  @Test
  void WHEN_usingJMSEndpoint_THEN_bridgeErrorHandlerIsFalse() {
    // arrange
    EndpointConsumerBuilder jmsEndpoint = StaticEndpointBuilders.jms("no-queue");

    // act
    EndpointConsumerBuilder endpointConsumerBuilder =
        DeclarativeHelper.resolveForbiddenEndpoint(jmsEndpoint);

    // assert
    assertThat(endpointConsumerBuilder.getUri()).contains("bridgeErrorHandler=false");
  }

  @Test
  void WHEN_usingClassWithGenericSuperclass_THEN_resolveGeneric() {

    // act
    Class<?> genericClass =
        DeclarativeReflectionUtils.getClassFromGeneric(
            MyExtendedIntegerList.class, ArrayList.class);

    // assert
    assertThat(genericClass.getTypeName()).isEqualTo("java.lang.Integer");
  }

  @Test
  void WHEN_isPrimaryEndpointWithGoodValues_THEN_true() {
    assertAll(
        () ->
            assertThat(
                    DeclarativeHelper.isPrimaryEndpoint(
                        ConnectorType.IN, RouteRole.EXTERNAL_ENDPOINT.getExternalName()))
                .isTrue(),
        () ->
            assertThat(
                    DeclarativeHelper.isPrimaryEndpoint(
                        ConnectorType.IN, RouteRole.EXTERNAL_SOAP_SERVICE_PROXY.getExternalName()))
                .isTrue(),
        () ->
            assertThat(
                    DeclarativeHelper.isPrimaryEndpoint(
                        ConnectorType.OUT, RouteRole.EXTERNAL_ENDPOINT.getExternalName()))
                .isTrue());
  }

  @Test
  void WHEN_isPrimaryEndpointWithBadValues_THEN_false() {
    assertAll(
        () ->
            assertThat(
                    DeclarativeHelper.isPrimaryEndpoint(
                        ConnectorType.IN,
                        RouteRole.CONNECTOR_REQUEST_ORCHESTRATION.getExternalName()))
                .isFalse(),
        () ->
            assertThat(
                    DeclarativeHelper.isPrimaryEndpoint(
                        ConnectorType.IN,
                        RouteRole.CONNECTOR_RESPONSE_ORCHESTRATION.getExternalName()))
                .isFalse(),
        () ->
            assertThat(
                    DeclarativeHelper.isPrimaryEndpoint(
                        ConnectorType.IN, RouteRole.SCENARIO_HANDOFF.getExternalName()))
                .isFalse(),
        () ->
            assertThat(
                    DeclarativeHelper.isPrimaryEndpoint(
                        ConnectorType.OUT,
                        RouteRole.CONNECTOR_REQUEST_ORCHESTRATION.getExternalName()))
                .isFalse(),
        () ->
            assertThat(
                    DeclarativeHelper.isPrimaryEndpoint(
                        ConnectorType.OUT,
                        RouteRole.CONNECTOR_RESPONSE_ORCHESTRATION.getExternalName()))
                .isFalse(),
        () ->
            assertThat(
                    DeclarativeHelper.isPrimaryEndpoint(
                        ConnectorType.OUT, RouteRole.SCENARIO_HANDOFF.getExternalName()))
                .isFalse(),
        () ->
            assertThat(
                    DeclarativeHelper.isPrimaryEndpoint(
                        ConnectorType.OUT, RouteRole.EXTERNAL_SOAP_SERVICE_PROXY.getExternalName()))
                .isFalse());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "http:localhost/{{address}}",
        "http:localhost/${address}",
        "http:localhost/{{address}}/${path}",
        "http:localhost/${path}/{{address}}"
      })
  void WHEN_CheckingURIWithPlaceholders_THEN_true(String uri) {
    assertThat(DeclarativeHelper.doesUriContainPlaceholders(uri)).isTrue();
  }

  @Test
  void WHEN_CheckingURI_WITH_NoPlaceholder_THEN_EvaluateAsFalse() {
    String uri = "http:localhost";
    assertThat(DeclarativeHelper.doesUriContainPlaceholders(uri)).isFalse();
  }
}
