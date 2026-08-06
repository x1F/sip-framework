package one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl;

import static one.x1f.sip.foundation.testkit.util.HttpInvokerHelper.createExchangeResponse;
import static one.x1f.sip.foundation.testkit.util.HttpInvokerHelper.prepareHeaders;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.testkit.util.TestKitHelper;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.RouteInvoker;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.component.rest.RestEndpoint;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/** Invoker class for triggering Camel REST route */
@Slf4j
@Component
@RequiredArgsConstructor
public class RestRouteInvoker implements RouteInvoker {

  private final CamelContext camelContext;
  private final Environment environment;
  private final RestClient.Builder restClientBuilder;

  @Value("${sip.adapter.camel-endpoint-context-path}")
  private String contextPath = "";

  @Override
  public Optional<Exchange> invoke(Exchange inputExchange) {
    Endpoint endpoint = TestKitHelper.resolveEndpoint(inputExchange, camelContext);
    var headers = prepareHeaders(inputExchange);
    HttpEntity<String> testRequest =
        new HttpEntity<>(inputExchange.getMessage().getBody(String.class), headers);
    log.trace("sip.testkit.workflow.whenphase.routeinvoker.rest.request_{}", testRequest);

    ResponseEntity<String> response =
        restClientBuilder
            .build()
            .method(resolveHttpMethod(endpoint))
            .uri(createUri(endpoint, headers))
            .body(testRequest)
            .retrieve()
            .toEntity(new ParameterizedTypeReference<>() {});
    log.trace("sip.testkit.workflow.whenphase.routeinvoker.rest.response_{}", response);

    return Optional.of(createExchangeResponse(response, camelContext));
  }

  @Override
  public boolean isApplicable(Endpoint endpoint) {
    return endpoint instanceof RestEndpoint;
  }

  private String createUri(Endpoint endpoint, HttpHeaders headers) {
    String resolvedUrl =
        UriComponentsBuilder.fromUriString(((RestEndpoint) endpoint).getPath())
            .buildAndExpand(headers.toSingleValueMap())
            .toUriString();
    return String.format(
        "http://localhost:%s%s/%s",
        environment.getProperty("local.server.port"), resolveContextPath(), resolvedUrl);
  }

  private String resolveContextPath() {
    return contextPath.replaceAll("/[*]$", "");
  }

  private HttpMethod resolveHttpMethod(Endpoint endpoint) {
    if (endpoint instanceof RestEndpoint restEndpoint
        && StringUtils.isNotEmpty(restEndpoint.getMethod())) {
      return HttpMethod.valueOf(restEndpoint.getMethod().toUpperCase());
    }
    return HttpMethod.POST;
  }
}
