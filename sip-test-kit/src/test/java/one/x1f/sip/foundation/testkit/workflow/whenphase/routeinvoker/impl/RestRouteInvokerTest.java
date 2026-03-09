package one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import one.x1f.sip.foundation.core.proxies.ProcessorProxy;
import one.x1f.sip.foundation.testkit.config.AdapterConfigurationProperties;
import one.x1f.sip.foundation.testkit.workflow.givenphase.Mock;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.RouteInvoker;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.component.rest.RestEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class RestRouteInvokerTest {

  private static final String ROUTE_ID = "routeId";
  public static final String TEST_RESPONSE = "test response";

  private RestRouteInvoker restRouteInvoker;
  private Exchange exchange;

  private RestTemplate restTemplate;

  private RestEndpoint restEndpoint;

  @BeforeEach
  void setUp() {

    CamelContext camelContext = mock(CamelContext.class);
    RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
    restTemplate = mock(RestTemplate.class);
    restRouteInvoker =
        new RestRouteInvoker(
            camelContext,
            mock(Environment.class),
            restTemplateBuilder,
            createAdapterConfigurationProperties());
    exchange = mock(Exchange.class, RETURNS_DEEP_STUBS);
    ResponseEntity<String> routeExpectedResponse =
        new ResponseEntity<>(TEST_RESPONSE, HttpStatus.OK);
    restEndpoint = mock(RestEndpoint.class);
    when(restTemplateBuilder.build()).thenReturn(restTemplate);
    when(restEndpoint.getMethod()).thenReturn("post");
    when(restEndpoint.getPath()).thenReturn("test");
    when(restTemplate.exchange(
            anyString(),
            any(HttpMethod.class),
            any(),
            ArgumentMatchers.<ParameterizedTypeReference<String>>any()))
        .thenReturn(routeExpectedResponse);

    when(exchange.getProperty(Mock.ENDPOINT_ID_EXCHANGE_PROPERTY)).thenReturn(ROUTE_ID);
    when(exchange.getMessage().getBody(String.class)).thenReturn("request");
    when(exchange.getMessage().getHeader(RouteInvoker.TEST_NAME_HEADER, String.class))
        .thenReturn("test");
    when(exchange.getMessage().getHeader(ProcessorProxy.TEST_MODE_HEADER, String.class))
        .thenReturn("true");
    Route route = mock(Route.class);
    when(camelContext.getRoute(ROUTE_ID)).thenReturn(route);
    when(route.getEndpoint()).thenReturn(restEndpoint);
  }

  @Test
  void GIVEN_mockedExchangeAndEndpoint_WHEN_executeTask_THEN_verifySendingToGoodEndpointUri() {
    // act
    Optional<Exchange> target = restRouteInvoker.invoke(exchange);

    // assert
    assertThat(target).isPresent();
    assertThat(target.get().getMessage().getBody(String.class)).isEqualTo(TEST_RESPONSE);
  }

  @Test
  void GIVEN_EndpointWithPOSTMethod_WHEN_executeTask_THEN_verifyPOSTmethodUsed() {
    // arrange
    when(restEndpoint.getMethod()).thenReturn("post");
    // act
    Optional<Exchange> target = restRouteInvoker.invoke(exchange);

    // assert
    assertThat(target).isPresent();
    verify(restTemplate)
        .exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(),
            ArgumentMatchers.<ParameterizedTypeReference<String>>any());
  }

  @Test
  void GIVEN_EndpointWithGETMethod_WHEN_executeTask_THEN_verifyGETmethodUsed() {
    // arrange
    when(restEndpoint.getMethod()).thenReturn("get");
    // act
    Optional<Exchange> target = restRouteInvoker.invoke(exchange);

    // assert
    assertThat(target).isPresent();
    verify(restTemplate)
        .exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(),
            ArgumentMatchers.<ParameterizedTypeReference<String>>any());
  }

  @Test
  void GIVEN_EndpointWithNullMethod_WHEN_executeTask_THEN_verifyPOSTmethodUsed() {
    // arrange
    when(restEndpoint.getMethod()).thenReturn(null);
    // act
    Optional<Exchange> target = restRouteInvoker.invoke(exchange);

    // assert
    assertThat(target).isPresent();
    verify(restTemplate)
        .exchange(
            anyString(),
            eq(HttpMethod.POST),
            any(),
            ArgumentMatchers.<ParameterizedTypeReference<String>>any());
  }

  @Test
  void GIVEN_EndpointWithPathParams_WHEN_executeTask_THEN_ParamsResolved() {
    // arrange
    when(restEndpoint.getMethod()).thenReturn("get");
    when(restEndpoint.getPath()).thenReturn("test/{headerKey}");
    Map<String, Object> headers = new HashMap<>();
    headers.put(RouteInvoker.TEST_NAME_HEADER, "test");
    headers.put(ProcessorProxy.TEST_MODE_HEADER, "true");
    headers.put("headerKey", "headerValue");
    when(exchange.getMessage().getHeaders()).thenReturn(headers);
    // act
    Optional<Exchange> target = restRouteInvoker.invoke(exchange);

    // assert
    assertThat(target).isPresent();
    verify(restTemplate)
        .exchange(
            contains("headerValue"),
            eq(HttpMethod.GET),
            any(),
            ArgumentMatchers.<ParameterizedTypeReference<String>>any());
  }

  private AdapterConfigurationProperties createAdapterConfigurationProperties() {
    var adapterConfigurationProperties = new AdapterConfigurationProperties();
    adapterConfigurationProperties.setCamelCxfEndpointContextPath("");
    adapterConfigurationProperties.setCamelEndpointContextPath("");
    return adapterConfigurationProperties;
  }
}
