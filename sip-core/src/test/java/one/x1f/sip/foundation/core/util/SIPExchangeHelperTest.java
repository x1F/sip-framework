package one.x1f.sip.foundation.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.*;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.converter.stream.InputStreamCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SIPExchangeHelperTest {

  private static final String SERIALIZABLE_DEFAULT_VALUE = "This is non serializable value";
  private static final String BODY = "body";

  private Exchange exchange;
  private Map<String, Object> stringObjectMap;
  private CamelContext camelContext;

  public record Person(String name, int years) {}

  @BeforeEach
  void setup() {
    camelContext = mock(CamelContext.class);
    exchange = mock(Exchange.class, RETURNS_DEEP_STUBS);
    stringObjectMap = new HashMap<>();
    Message message = mock(Message.class);
    when(exchange.getMessage()).thenReturn(message);
    when(message.getBody()).thenReturn(BODY);
    when(message.getHeaders()).thenReturn(stringObjectMap);
    when(exchange.getProperties()).thenReturn(stringObjectMap);
    when(exchange.getExchangeExtension().getInternalProperties()).thenReturn(stringObjectMap);
  }

  @Test
  void GIVEN_differentHeaderValues_WHEN_filterNonSerializableHeaders_THEN_getOnlyFilteredHeaders() {

    stringObjectMap.put("empty", null);
    stringObjectMap.put("nonempty", "sth");

    Map<String, Object> result = SIPExchangeHelper.filterNonSerializableHeaders(exchange);

    assertThat(result.get("empty")).isNull();
    assertThat(result.get("nonempty")).isNotNull();
  }

  @Test
  void
      GIVEN_nonSerializableValue_WHEN_reassignNonSerializableValue_THEN_expectSerializableDefaultValue() {
    // arrange
    ExchangeBuilder exchangeBuilder = ExchangeBuilder.anExchange(camelContext);
    Exchange nonserializableValue = exchangeBuilder.build();

    // act
    String actual =
        (String) SIPExchangeHelper.reassignNonSerializableValue("test", nonserializableValue);

    // assert
    assertThat(actual).isEqualTo(SERIALIZABLE_DEFAULT_VALUE);
  }

  @Test
  void
      GIVEN_differentPropertyValues_WHEN_filterNonSerializableProperties_THEN_getOnlyFilteredProperties() {
    stringObjectMap.put("empty", null);
    stringObjectMap.put("nonempty", "sth");

    Map<String, Object> result = SIPExchangeHelper.filterNonSerializableProperties(exchange);

    assertThat(result.get("empty")).isNull();
    assertThat(result.get("nonempty")).isNotNull();
  }

  @Test
  void
      GIVEN_differentInternalPropertyValues_WHEN_filterNonSerializableInternalProperties_THEN_getOnlyFilteredInternalProperties() {
    stringObjectMap.put("empty", null);
    stringObjectMap.put("nonempty", "sth");

    Map<String, Object> result =
        SIPExchangeHelper.filterNonSerializableInternalProperties(exchange);

    assertThat(result.get("empty")).isNull();
    assertThat(result.get("nonempty")).isNotNull();
  }

  @Test
  void
      GIVEN_stringBody_WHEN_extractBodyAsJsonString_THEN_stringBodyIsReturnedDirectlyWithoutConversion() {
    // act
    String result = SIPExchangeHelper.extractBodyAsJsonString(exchange.getMessage());
    // assert
    assertThat(result).isEqualTo(BODY);
  }

  @Test
  void
      GIVEN_objectBody_WHEN_extractMarshalledBodyAsJsonString_THEN_stringBodyIsReturnedDirectlyWithConversion() {
    // arrange
    Message message = exchange.getMessage();
    TypeConverter typeConverter = mock(TypeConverter.class);
    Person person = new Person("John", 40);
    when(message.getBody()).thenReturn(person);
    when(message.getExchange()).thenReturn(exchange);
    when(exchange.getContext()).thenReturn(camelContext);
    when(camelContext.getTypeConverter()).thenReturn(typeConverter);
    InputStreamCache streamCache = new InputStreamCache(BODY.getBytes(StandardCharsets.UTF_8));
    when(typeConverter.tryConvertTo(eq(StreamCache.class), any(), any())).thenReturn(streamCache);
    // act
    String result = SIPExchangeHelper.extractBodyAsJsonString(message);
    // assert
    assertThat(result).isEqualTo(BODY);
    verify(message).setBody(streamCache);
  }

  @Test
  void
      GIVEN_objectBody_WHEN_extractBodyAsJsonString_THEN_stringBodyIsReturnedDirectlyWithConversion() {
    // arrange
    Message message = exchange.getMessage();
    TypeConverter typeConverter = mock(TypeConverter.class);
    Person person = new Person("John", 40);
    when(message.getBody()).thenReturn(person);
    when(message.getExchange()).thenReturn(exchange);
    when(exchange.getContext()).thenReturn(camelContext);
    when(camelContext.getTypeConverter()).thenReturn(typeConverter);
    when(typeConverter.tryConvertTo(eq(StreamCache.class), any(), any())).thenReturn(null);

    // act
    String result = SIPExchangeHelper.extractBodyAsJsonString(message);
    // assert
    assertThat(result).contains("name", "John", "year", "40");
  }
}
