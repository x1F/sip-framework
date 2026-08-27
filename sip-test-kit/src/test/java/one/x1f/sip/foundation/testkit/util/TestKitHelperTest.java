package one.x1f.sip.foundation.testkit.util;

import static one.x1f.sip.foundation.core.proxies.ProcessorProxy.TEST_MODE_HEADER;
import static one.x1f.sip.foundation.testkit.util.TestKitHelper.*;
import static one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.RouteInvoker.TEST_NAME_HEADER;
import static one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl.DirectRouteInvokerTest.JSON_MODEL_PAYLOAD_BODY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;
import one.x1f.sip.foundation.testkit.configurationproperties.models.EndpointProperties;
import one.x1f.sip.foundation.testkit.configurationproperties.models.MessageProperties;
import one.x1f.sip.foundation.testkit.workflow.givenphase.Mock;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl.DirectRouteInvokerTest;
import org.apache.camel.*;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.*;
import org.apache.camel.model.dataformat.JsonDataFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TestKitHelperTest {

  private static final String BODY = "body";
  private static final String ROUTE_ID = "routeId";

  private Exchange exchange;
  private CamelContext camelContext;

  @BeforeEach
  void setup() {
    camelContext = mock(CamelContext.class);
    when(camelContext.getCamelContextExtension()).thenReturn(mock(ExtendedCamelContext.class));
    exchange = mock(Exchange.class);
    Map<String, Object> headers = new HashMap<>();
    Message message = mock(Message.class);
    when(exchange.getMessage()).thenReturn(message);
    when(message.getBody()).thenReturn(BODY);
    when(message.getHeaders()).thenReturn(headers);
  }

  @Test
  void GIVEN_simpleBody_WHEN_mapToMessageProperties_THEN_expectValidBody() {
    Exchange mockExchange = mock(Exchange.class);
    Map<String, Object> headers = new HashMap<>();
    Message message = mock(Message.class);
    when(mockExchange.getMessage()).thenReturn(message);
    when(message.getBody()).thenReturn(BODY);
    when(message.getHeaders()).thenReturn(headers);

    MessageProperties actual = MessageProperties.mapToMessageProperties(mockExchange);

    assertThat(actual.getBody()).isEqualTo(BODY);
    assertThat(actual.getHeaders()).isEqualTo(headers);
  }

  @Test
  void GIVEN_route_WHEN_resolveEndpoint_THEN_expectEndpoint() {
    // assert
    Route route = mock(Route.class);
    Endpoint expectedEndpoint = mock(Endpoint.class);
    when(camelContext.getRoute(ROUTE_ID)).thenReturn(route);
    when(route.getEndpoint()).thenReturn(expectedEndpoint);
    when(exchange.getProperty(Mock.ENDPOINT_ID_EXCHANGE_PROPERTY, String.class))
        .thenReturn(ROUTE_ID);

    // act
    Endpoint actualEndpoint = resolveEndpoint(exchange, camelContext);

    // arrange
    assertThat(actualEndpoint).isEqualTo(expectedEndpoint);
  }

  @Test
  void GIVEN_noRoute_WHEN_resolveEndpoint_THEN_expectSIPFrameworkException() {
    // act & arrange
    assertThrows(SIPFrameworkException.class, () -> resolveEndpoint(exchange, camelContext));
  }

  @Test
  void GIVEN_routeId_WHEN_resolveConsumer_THEN_expectConsumer() {
    // assert
    Route route = mock(Route.class);
    Consumer expectedConsumer = mock(Consumer.class);
    when(camelContext.getRoute(ROUTE_ID)).thenReturn(route);
    when(route.getConsumer()).thenReturn(expectedConsumer);
    when(exchange.getProperty(Mock.ENDPOINT_ID_EXCHANGE_PROPERTY, String.class))
        .thenReturn(ROUTE_ID);

    // act
    Consumer actualConsumer = resolveConsumer(exchange, camelContext);

    // arrange
    assertThat(actualConsumer).isEqualTo(expectedConsumer);
  }

  @Test
  void GIVEN_properties_WHEN_parseExchangeProperties_THEN_expectExchangeWithValues() {
    // assert
    EndpointProperties properties = new EndpointProperties();
    properties.setEndpointId("routeId");
    MessageProperties messageProperties = new MessageProperties();
    messageProperties.setBody("body");
    messageProperties.setHeaders(Map.of("headerKey", "value"));
    properties.setRequestMessage(messageProperties);

    // act
    Exchange actual = parseExchangeProperties(properties, camelContext);

    // arrange
    assertThat(actual.getMessage().getBody()).isEqualTo("body");
    assertThat(actual.getMessage().getHeaders()).containsEntry("headerKey", "value");
  }

  @Test
  void GIVEN_noProperties_WHEN_parseExchangeProperties_THEN_expectExchangeWithValues() {
    // act
    Exchange actual = parseExchangeProperties(null, camelContext);

    // arrange
    assertThat(actual.getMessage().getBody()).isNull();
    assertThat(actual.getMessage().getHeaders()).isEmpty();
  }

  @ParameterizedTest(name = "Using input headerKey: {0}")
  @ValueSource(strings = {TEST_NAME_HEADER, TEST_MODE_HEADER})
  void GIVEN_sipTestKitHeader_WHEN_isTestKitHeader_THEN_expectTrue(String headerKey) {
    // act + assert
    assertThat(isTestKitHeader(headerKey)).isTrue();
  }

  @Test
  void GIVEN_customHeader_WHEN_isTestKitHeader_THEN_expectFalse() {
    // act + assert
    assertThat(isTestKitHeader("customHeaderKey")).isFalse();
  }

  @Test
  void GIVEN_PersonJsonRequestModel_WHEN_unmarshallExchangeBodyFromJson_THEN_expectPersonPojo() {
    // arrange
    Exchange mockExchange = TestKitHelper.parseExchangeProperties(null, camelContext);
    mockExchange.getMessage().setBody(JSON_MODEL_PAYLOAD_BODY);

    // act
    unmarshallExchangeBodyFromJson(
        mockExchange, new ObjectMapper(), DirectRouteInvokerTest.Person.class);

    // assert
    assertThat(mockExchange.getMessage().getBody())
        .isInstanceOf(DirectRouteInvokerTest.Person.class);
  }

  @Test
  void
      GIVEN_NoJsonRequestModel_WHEN_unmarshallExchangeBodyFromJson_THEN_expectSIPFrameworkException() {
    // arrange
    Exchange mockExchange = TestKitHelper.parseExchangeProperties(null, camelContext);
    mockExchange.getMessage().setBody("string value");

    // act && assert
    assertThatThrownBy(
            () -> {
              unmarshallExchangeBodyFromJson(
                  mockExchange, new ObjectMapper(), DirectRouteInvokerTest.Person.class);
            })
        .isInstanceOf(SIPFrameworkException.class);
  }

  @Test
  void
      GIVEN_choiceContainingJsonDslUnmarshal_WHEN_extractUnmarshalClass_THEN_returnsUnmarshalType() {
    JsonDataFormat jsonDf = mock(JsonDataFormat.class);
    when(jsonDf.getUnmarshalType()).thenReturn((Class) MyModel.class);

    UnmarshalDefinition unmarshalDef = mock(UnmarshalDefinition.class);
    when(unmarshalDef.getDataFormatType()).thenReturn(jsonDf);

    ChoiceDefinition choiceDef = mock(ChoiceDefinition.class);
    when(choiceDef.getOutputs()).thenReturn(List.<ProcessorDefinition<?>>of(unmarshalDef));

    RouteDefinition routeDef = mock(RouteDefinition.class);
    when(routeDef.getOutputs()).thenReturn(List.<ProcessorDefinition<?>>of(choiceDef));

    Class<?> result = TestKitHelper.extractUnmarshalClass(routeDef);

    assertThat(result).isEqualTo(MyModel.class);
  }

  @Test
  void
      GIVEN_choiceContainingRawJacksonDataFormatUnmarshal_WHEN_extractUnmarshalClass_THEN_returnsUnmarshalType() {
    JacksonDataFormat jacksonDf = mock(JacksonDataFormat.class);
    when(jacksonDf.getUnmarshalType()).thenReturn((Class) MyModel.class);

    DataFormatDefinition dfDef = mock(DataFormatDefinition.class);
    when(dfDef.getDataFormat()).thenReturn(jacksonDf);

    UnmarshalDefinition unmarshalDef = mock(UnmarshalDefinition.class);
    when(unmarshalDef.getDataFormatType()).thenReturn(dfDef);

    ChoiceDefinition choiceDef = mock(ChoiceDefinition.class);
    when(choiceDef.getOutputs()).thenReturn(List.<ProcessorDefinition<?>>of(unmarshalDef));

    RouteDefinition routeDef = mock(RouteDefinition.class);
    when(routeDef.getOutputs()).thenReturn(List.<ProcessorDefinition<?>>of(choiceDef));

    Class<?> result = TestKitHelper.extractUnmarshalClass(routeDef);

    assertThat(result).isEqualTo(MyModel.class);
  }

  @Test
  void GIVEN_routeWithoutChoice_WHEN_extractUnmarshalClass_THEN_returnsNull() {
    LogDefinition logDef = mock(LogDefinition.class);

    RouteDefinition routeDef = mock(RouteDefinition.class);
    when(routeDef.getOutputs()).thenReturn(List.<ProcessorDefinition<?>>of(logDef));

    Class<?> result = TestKitHelper.extractUnmarshalClass(routeDef);

    assertThat(result).isNull();
  }

  @Test
  void GIVEN_choiceWithoutUnmarshal_WHEN_extractUnmarshalClass_THEN_returnsNull() {
    LogDefinition logDef = mock(LogDefinition.class);

    ChoiceDefinition choiceDef = mock(ChoiceDefinition.class);
    when(choiceDef.getOutputs()).thenReturn(List.<ProcessorDefinition<?>>of(logDef));

    RouteDefinition routeDef = mock(RouteDefinition.class);
    when(routeDef.getOutputs()).thenReturn(List.<ProcessorDefinition<?>>of(choiceDef));

    Class<?> result = TestKitHelper.extractUnmarshalClass(routeDef);

    assertThat(result).isNull();
  }

  @Test
  void GIVEN_outputsContainingUnmarshalDefinition_WHEN_findUnmarshalType_THEN_returnsUnmarshalType()
      throws Exception {
    JsonDataFormat jsonDf = mock(JsonDataFormat.class);
    when(jsonDf.getUnmarshalType()).thenReturn((Class) MyModel.class);

    UnmarshalDefinition unmarshalDef = mock(UnmarshalDefinition.class);
    when(unmarshalDef.getDataFormatType()).thenReturn(jsonDf);

    List<ProcessorDefinition<?>> outputs = List.of(unmarshalDef);

    Class<?> result = invokeFindUnmarshalType(outputs);

    assertThat(result).isEqualTo(MyModel.class);
  }

  @Test
  void GIVEN_emptyOutputs_WHEN_findUnmarshalType_THEN_returnsNull() throws Exception {
    Class<?> result = invokeFindUnmarshalType(List.of());

    assertThat(result).isNull();
  }

  @Test
  void GIVEN_outputsWithoutUnmarshalDefinition_WHEN_findUnmarshalType_THEN_returnsNull()
      throws Exception {
    LogDefinition logDef = mock(LogDefinition.class);

    List<ProcessorDefinition<?>> outputs = List.of(logDef);

    Class<?> result = invokeFindUnmarshalType(outputs);

    assertThat(result).isNull();
  }

  @Test
  void GIVEN_jsonDataFormatModel_WHEN_extractUnmarshalType_THEN_returnsUnmarshalType()
      throws Exception {
    JsonDataFormat jsonDf = mock(JsonDataFormat.class);
    when(jsonDf.getUnmarshalType()).thenReturn((Class) MyModel.class);

    Class<?> result = invokeExtractUnmarshalType(jsonDf);

    assertThat(result).isEqualTo(MyModel.class);
  }

  @Test
  void
      GIVEN_dataFormatDefinitionWrappingJacksonDataFormat_WHEN_extractUnmarshalType_THEN_returnsUnmarshalType()
          throws Exception {
    JacksonDataFormat jacksonDf = mock(JacksonDataFormat.class);
    when(jacksonDf.getUnmarshalType()).thenReturn((Class) MyModel.class);

    DataFormatDefinition dfDef = mock(DataFormatDefinition.class);
    when(dfDef.getDataFormat()).thenReturn(jacksonDf);

    Class<?> result = invokeExtractUnmarshalType(dfDef);

    assertThat(result).isEqualTo(MyModel.class);
  }

  @Test
  void
      GIVEN_arrayUnmarshalType_WHEN_extractUnmarshalType_THEN_returnsArrayClassWithMatchingComponentType()
          throws Exception {
    JacksonDataFormat jacksonDf = mock(JacksonDataFormat.class);
    when(jacksonDf.getUnmarshalType()).thenReturn((Class) MyModel[].class);

    DataFormatDefinition dfDef = mock(DataFormatDefinition.class);
    when(dfDef.getDataFormat()).thenReturn(jacksonDf);

    Class<?> result = invokeExtractUnmarshalType(dfDef);

    assertThat(result).isEqualTo(MyModel[].class);
    assertThat(result.getComponentType()).isEqualTo(MyModel.class);
  }

  @Test
  void GIVEN_nullDataFormatDefinition_WHEN_extractUnmarshalType_THEN_returnsNull()
      throws Exception {
    Class<?> result = invokeExtractUnmarshalType(null);

    assertThat(result).isNull();
  }

  @Test
  void GIVEN_unrelatedDataFormatDefinition_WHEN_extractUnmarshalType_THEN_returnsNull()
      throws Exception {
    DataFormatDefinition dfDef = mock(DataFormatDefinition.class);
    when(dfDef.getDataFormat()).thenReturn(null);

    Class<?> result = invokeExtractUnmarshalType(dfDef);

    assertThat(result).isNull();
  }

  private Class<?> invokeFindUnmarshalType(List<ProcessorDefinition<?>> outputs) throws Exception {
    Method m = TestKitHelper.class.getDeclaredMethod("findUnmarshalType", List.class);
    m.setAccessible(true);
    return (Class<?>) m.invoke(null, outputs);
  }

  private Class<?> invokeExtractUnmarshalType(DataFormatDefinition dfDef) throws Exception {
    Method m =
        TestKitHelper.class.getDeclaredMethod("extractUnmarshalType", DataFormatDefinition.class);
    m.setAccessible(true);
    return (Class<?>) m.invoke(null, dfDef);
  }

  static class MyModel {
    private String field;

    public String getField() {
      return field;
    }

    public void setField(String field) {
      this.field = field;
    }
  }
}
