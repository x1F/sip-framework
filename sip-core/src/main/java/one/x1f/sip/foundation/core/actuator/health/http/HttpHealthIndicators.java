package one.x1f.sip.foundation.core.actuator.health.http;

import static one.x1f.sip.foundation.core.declarative.utils.DeclarativeHelper.appendMetadata;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Endpoint;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.HttpStatus;

/**
 * {@link HttpHealthIndicators} contains several functions that implement health checks for HTTP
 * endpoints.
 */
@Slf4j
public class HttpHealthIndicators {
  private static final int DEFAULT_HTTP_TIMEOUT = 30000;

  private HttpHealthIndicators() {}

  /**
   * A dummy health function that always returns health status UP.
   *
   * @return {@link Health}
   */
  public static Health alwaysHealthy() {
    return new Health.Builder().up().build();
  }

  /**
   * Returns {@link Health} information based on the the URL from the Endpoint.
   *
   * @param endpoint Endpoint singleton
   * @return Returns health status UNKNOWN for every detected endpoint if not further specified
   */
  public static Health alwaysUnknown(Endpoint endpoint) {
    return new Health.Builder().withDetails(createDetails(endpoint)).unknown().build();
  }

  private static Map<String, Object> createDetails(Endpoint endpoint) {
    Map<String, Object> details = new HashMap<>();
    details.put("url", endpoint.getEndpointKey());
    return appendMetadata(endpoint, details);
  }

  /**
   * Returns {@link Health} information based on the ability to perform GET HTTP request using the
   * URL from the Endpoint.
   *
   * @param endpoint Endpoint singleton
   * @return Returns health status UP if it successfully execute the request, UNKNOWN if HTTP
   *     response code is not 2xx, or DOWN if request fails for any other reason.
   */
  public static Health urlHealthIndicator(Endpoint endpoint) {
    return urlHealthStatus(endpoint, DEFAULT_HTTP_TIMEOUT);
  }

  private static Health urlHealthStatus(Endpoint endpoint, int timeout) {
    String url = endpoint.getEndpointKey();
    Health.Builder builder = new Health.Builder();
    builder.withDetails(createDetails(endpoint));
    try {
      HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
      connection.setConnectTimeout(timeout);
      connection.connect();
      Optional<HttpStatus> httpStatus =
          Optional.ofNullable(HttpStatus.resolve(connection.getResponseCode()));

      httpStatus.ifPresent(
          status -> {
            if (status.is2xxSuccessful()) {
              builder.up();
            } else {
              builder
                  .unknown()
                  .withDetail("status", status)
                  .withDetail("statusCode", status.value());
            }
          });
    } catch (IOException e) {
      builder.down(e);
    }
    return builder.build();
  }
}
