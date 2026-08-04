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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.client.RestClient;

class CxfRouteInvokerTest {

  private static final String ROUTE_ID = "routeId";
  private static final String RESPONSE_BODY =
      "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><ns2:AddBookResponse xmlns:ns2=\"http://www.cleverbuilder.com/BookService/\"><ns2:Book><ID>1</ID><Title>Camel in Action</Title><Author>Claus Ibsen</Author></ns2:Book></ns2:AddBookResponse></soap:Body></soap:Envelope>";

  private CamelContext camelContext;

  @BeforeEach
  void setup() {
    camelContext = mock(CamelContext.class);
  }

  @SuppressWarnings("unchecked")
  @ParameterizedTest(name = "Using input body: {0}")
  @ValueSource(strings = {RESPONSE_BODY})
  @NullSource
  void GIVEN_emptyRequest_WHEN_executeTask_THEN_validateGoodResponse(String inputBody) {
    // arrange
    RestClient.Builder restClientBuilder = mock(RestClient.Builder.class);
    RestClient restClient = mock(RestClient.class);
    RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
    RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
    RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
    Environment environment = mock(Environment.class);
    Route route = mock(Route.class);

    ResponseEntity<String> routeExpectedResponse = new ResponseEntity<>(inputBody, HttpStatus.OK);

    when(restClientBuilder.build()).thenReturn(restClient);
    when(restClient.method(any(HttpMethod.class))).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
    when(requestBodySpec.body(any(HttpEntity.class))).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.toEntity(any(ParameterizedTypeReference.class)))
        .thenReturn(routeExpectedResponse);
    when(camelContext.getCamelContextExtension()).thenReturn(mock(ExtendedCamelContext.class));
    when(camelContext.getRoute(ROUTE_ID)).thenReturn(route);
    when(route.getEndpoint()).thenReturn(mock(Endpoint.class));
    when(environment.getProperty("local.server.port")).thenReturn("8081");
    when(restClientBuilder.build()).thenReturn(restClient);

    CxfRouteInvoker subject = new CxfRouteInvoker(camelContext, environment, restClientBuilder);

    Exchange exchange = createExchange(new HashMap<>());
    CxfRouteInvoker spySubject = spy(subject);
    doReturn("test").when(spySubject).getCxfEndpointAddress(any());

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
