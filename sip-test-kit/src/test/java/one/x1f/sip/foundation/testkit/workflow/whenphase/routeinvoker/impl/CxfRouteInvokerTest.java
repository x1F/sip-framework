package one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import one.x1f.sip.foundation.testkit.workflow.givenphase.Mock;
import org.apache.camel.*;
import org.apache.camel.builder.ExchangeBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

class CxfRouteInvokerTest {

  private static final String ROUTE_ID = "routeId";
  private static final String RESPONSE_BODY =
      "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><ns2:AddBookResponse xmlns:ns2=\"http://www.cleverbuilder.com/BookService/\"><ns2:Book><ID>1</ID><Title>Camel in Action</Title><Author>Claus Ibsen</Author></ns2:Book></ns2:AddBookResponse></soap:Body></soap:Envelope>";

  private CxfRouteInvoker subject;
  private RestTemplate restTemplate;
  private CamelContext camelContext;

  @BeforeEach
  void setup() {
    camelContext = mock(CamelContext.class);
    RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
    restTemplate = mock(RestTemplate.class);
    Environment environment = mock(Environment.class);
    subject = new CxfRouteInvoker(camelContext, environment, restTemplateBuilder);

    Route route = mock(Route.class);
    when(camelContext.getCamelContextExtension()).thenReturn(mock(ExtendedCamelContext.class));
    when(camelContext.getRoute(ROUTE_ID)).thenReturn(route);
    when(route.getEndpoint()).thenReturn(mock(Endpoint.class));

    when(environment.getProperty("local.server.port")).thenReturn("8081");
    when(restTemplateBuilder.build()).thenReturn(restTemplate);
  }

  @SuppressWarnings("unchecked")
  @ParameterizedTest(name = "Using input body: {0}")
  @ValueSource(strings = {RESPONSE_BODY})
  @NullSource
  void GIVEN_emptyRequest_WHEN_executeTask_THEN_validateGoodResponse(String inputBody) {
    // arrange
    Exchange exchange = createExchange(new HashMap<>());
    CxfRouteInvoker spySubject = spy(subject);
    doReturn("test").when(spySubject).getCxfEndpointAddress(any());
    ResponseEntity<String> routeExpectedResponse = new ResponseEntity<>(inputBody, HttpStatus.OK);
    when(restTemplate.exchange(
            anyString(),
            any(HttpMethod.class),
            any(),
            ArgumentMatchers.<ParameterizedTypeReference>any()))
        .thenReturn(routeExpectedResponse);

    // act
    Optional<Exchange> actualExchange = spySubject.invoke(exchange);

    // assert
    actualExchange.ifPresent(
        value -> assertThat(value.getMessage().getBody()).isEqualTo(inputBody));
  }

  private Exchange createExchange(Map<String, Object> headers) {
    ExchangeBuilder exchangeBuilder = ExchangeBuilder.anExchange(camelContext).withBody("");
    headers.forEach(exchangeBuilder::withHeader);
    exchangeBuilder.withProperty(Mock.ENDPOINT_ID_EXCHANGE_PROPERTY, ROUTE_ID);
    return exchangeBuilder.build();
  }
}
