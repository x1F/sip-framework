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
import org.apache.camel.component.cxf.jaxws.CxfComponent;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/** Invoker class for triggering Camel CXF(SOAP) route */
@Component
@ConditionalOnClass(CxfComponent.class)
@RequiredArgsConstructor
@Slf4j
public class CxfRouteInvoker implements RouteInvoker {

  private final CamelContext camelContext;
  private final Environment environment;
  private final RestTemplateBuilder restTemplateBuilder;

  @Value("${sip.adapter.camel-cxf-endpoint-context-path}")
  private String cxfContextPath = "";

  @Override
  public Optional<Exchange> invoke(Exchange inputExchange) {
    Endpoint endpoint = TestKitHelper.resolveEndpoint(inputExchange, camelContext);
    HttpEntity<String> request =
        new HttpEntity<>(
            inputExchange.getMessage().getBody(String.class), prepareHeaders(inputExchange));
    log.trace("sip.testkit.workflow.whenphase.routeinvoker.soap.request_{}", request);

    ResponseEntity<String> response =
        restTemplateBuilder
            .build()
            .exchange(
                createAddressUri(endpoint),
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {});
    log.trace("sip.testkit.workflow.whenphase.routeinvoker.soap.response_{}", response);

    return Optional.of(createExchangeResponse(response, camelContext));
  }

  @Override
  public boolean isApplicable(Endpoint endpoint) {
    return endpoint instanceof CxfEndpoint;
  }

  private String createAddressUri(Endpoint endpoint) {
    return String.format(
        "http://localhost:%s%s/%s",
        environment.getProperty("local.server.port"),
        trimAddressSuffix(cxfContextPath),
        trimAddressPrefix(getCxfEndpointAddress(endpoint)));
  }

  private String trimAddressSuffix(String address) {
    return address.replaceAll("/$", "");
  }

  private String trimAddressPrefix(String address) {
    return address.replaceFirst("/", "");
  }

  String getCxfEndpointAddress(Endpoint endpoint) {
    return ((CxfEndpoint) endpoint).getAddress();
  }
}
