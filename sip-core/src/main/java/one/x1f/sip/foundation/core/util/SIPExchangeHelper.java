package one.x1f.sip.foundation.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.StreamCache;
import org.apache.camel.support.DefaultExchangeHolder;

/** Utility class that changes the {@link Exchange} */
@Slf4j
public class SIPExchangeHelper extends DefaultExchangeHolder {

  private static final String SERIALIZABLE_DEFAULT_VALUE = "This is non serializable value";

  private static final ObjectMapper OBJECT_MAPPER;

  static {
    OBJECT_MAPPER = new ObjectMapper();
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
  }

  /**
   * Filters out all non-serializable headers so they can be used in serializable environment
   *
   * @param exchange whose headers should be checked
   * @return Map of headers that are serializable
   */
  public static Map<String, Object> filterNonSerializableHeaders(Exchange exchange) {
    Map<String, Object> filteredHeaders = new HashMap<>();
    exchange
        .getMessage()
        .getHeaders()
        .forEach(
            (k, v) -> {
              Object value = getValidHeaderValue(k, v, true);
              if (value != null) {
                filteredHeaders.put(k, reassignNonSerializableValue(k, value));
              }
            });
    return filteredHeaders;
  }

  public static Map<String, Object> filterNonSerializableProperties(Exchange exchange) {
    Map<String, Object> filteredProperties = new HashMap<>();
    exchange
        .getProperties()
        .forEach(
            (k, v) -> {
              Object value = getValidExchangePropertyValue(k, v, true);
              if (value != null) {
                filteredProperties.put(k, reassignNonSerializableValue(k, value));
              }
            });
    return filteredProperties;
  }

  public static Map<String, Object> filterNonSerializableInternalProperties(Exchange exchange) {
    Map<String, Object> filteredProperties = new HashMap<>();
    exchange
        .getExchangeExtension()
        .getInternalProperties()
        .forEach(
            (k, v) -> {
              Object value = getValidExchangePropertyValue(k, v, true);
              if (value != null) {
                filteredProperties.put(k, reassignNonSerializableValue(k, value));
              }
            });
    return filteredProperties;
  }

  public static Object reassignNonSerializableValue(String headerName, Object value) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      objectMapper.writeValue(new ByteArrayOutputStream(), value);
    } catch (IOException e) {
      log.debug("sip.core.util.nonserializablevalue_{}", headerName);
      return SERIALIZABLE_DEFAULT_VALUE;
    }
    return value;
  }

  public static String extractBodyAsJsonString(Message message) {
    if (message == null) {
      return null;
    }

    Object body = message.getBody();

    if (body == null) {
      return null;
    }

    if (body instanceof String str) {
      return str;
    }

    StreamCache streamCache =
        message
            .getExchange()
            .getContext()
            .getTypeConverter()
            .tryConvertTo(StreamCache.class, message.getExchange(), body);

    if (streamCache != null) {
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        streamCache.writeTo(baos);
        byte[] content = baos.toByteArray();
        return new String(content, StandardCharsets.UTF_8);
      } catch (IOException e) {
        return String.format("Failed to extract body: %s", e.getMessage());
      } finally {
        streamCache.reset();
        message.setBody(streamCache); // Ensure original body is preserved
      }
    }

    try {
      String bodyAsString = OBJECT_MAPPER.writeValueAsString(message.getBody());
      if (bodyAsString != null) {
        return bodyAsString;
      }
    } catch (Exception ignored) {
      // ignored
    }

    return "Unsupported body type or empty body.";
  }
}
