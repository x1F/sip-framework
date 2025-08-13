package one.x1f.sip.foundation.testkit.util;

import static one.x1f.sip.foundation.core.proxies.ProcessorProxy.TEST_MODE_HEADER;
import static one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.RouteInvoker.TEST_NAME_HEADER;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.ExchangeBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

/** Helper class with common methods for rest and soap invokers */
public class HttpInvokerHelper {

  private HttpInvokerHelper() {}

  /**
   * Creates headers for test kit execution
   *
   * @param exchange {@link Exchange} from which data is extracted
   * @return Map with test kit headers
   */
  public static MultiValueMap<String, String> prepareHeaders(Exchange exchange) {
    MultiValueMap<String, String> headers = new HttpHeaders();
    Map<String, Object> existingHeaders = exchange.getMessage().getHeaders();

    existingHeaders.forEach(
        (key, value) -> {
          if (value instanceof List) {
            ((List<?>) value).forEach(val -> headers.add(key, val.toString()));
          } else {
            headers.add(key, value.toString());
          }
        });
    addHeaderIfAbsent(
        headers, TEST_NAME_HEADER, exchange.getProperty(TEST_NAME_HEADER, String.class));
    addHeaderIfAbsent(
        headers, TEST_MODE_HEADER, exchange.getProperty(TEST_MODE_HEADER, String.class));
    return headers;
  }

  private static void addHeaderIfAbsent(
      MultiValueMap<String, String> headers, String headerName, String headerValue) {
    if (!headers.containsKey(headerName) && headerValue != null) {
      headers.add(headerName, headerValue);
    }
  }

  /**
   * Create {@link Exchange} with a response recovered from test execution
   *
   * @param response {@link ResponseEntity}asd98 of test execution
   * @param camelContext {@link CamelContext}
   * @return {@link Exchange} with response
   */
  public static Exchange createExchangeResponse(
      ResponseEntity<String> response, CamelContext camelContext) {
    ExchangeBuilder exchangeBuilder =
        ExchangeBuilder.anExchange(camelContext).withBody(formatToOneLine(response.getBody()));
    response.getHeaders().forEach(exchangeBuilder::withHeader);
    exchangeBuilder.withProperty(TEST_MODE_HEADER, true);
    exchangeBuilder.withProperty(
        TEST_NAME_HEADER, response.getHeaders().getFirst(TEST_NAME_HEADER));
    return exchangeBuilder.build();
  }

  private static String formatToOneLine(String multilineString) {
    if (multilineString != null) {
      return multilineString.lines().map(String::strip).collect(Collectors.joining(""));
    }
    return null;
  }
}
