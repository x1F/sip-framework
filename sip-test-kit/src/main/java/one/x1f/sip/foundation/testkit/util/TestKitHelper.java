package one.x1f.sip.foundation.testkit.util;

import static one.x1f.sip.foundation.core.declarative.RoutesRegistry.SIP_ENDPOINT_PROCESSOR_SUFFIX;
import static one.x1f.sip.foundation.core.proxies.ProcessorProxy.TEST_MODE_HEADER;
import static one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.RouteInvoker.TEST_NAME_HEADER;
import static one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl.DirectRouteInvoker.CONNECTOR_ID_EXCHANGE_PROPERTY;
import static org.apache.camel.builder.ExchangeBuilder.anExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.core.util.SIPExchangeHelper;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;
import one.x1f.sip.foundation.testkit.configurationproperties.models.EndpointProperties;
import one.x1f.sip.foundation.testkit.configurationproperties.models.PayloadProperties;
import one.x1f.sip.foundation.testkit.workflow.givenphase.Mock;
import one.x1f.sip.foundation.testkit.workflow.thenphase.result.ValidationResult;
import org.apache.camel.*;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

/** Utility class that changes the {@link Exchange} */
@Slf4j
public class TestKitHelper extends SIPExchangeHelper {

  public static final String EVAL_PREFIX = "eval:";
  private static final ObjectMapper OBJECT_MAPPER;

  static {
    OBJECT_MAPPER = new ObjectMapper();
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
  }

  /**
   * Get route id from the {@link Exchange}
   *
   * @param exchange that should be mapped
   * @return route id
   */
  public static String getRouteId(Exchange exchange) {
    var endpointId = exchange.getProperty(Mock.ENDPOINT_ID_EXCHANGE_PROPERTY, String.class);
    return endpointId != null ? endpointId.replace(SIP_ENDPOINT_PROCESSOR_SUFFIX, "") : null;
  }

  /**
   * Get camel endpoint based on exchange route id
   *
   * @param exchange for fetching the route id
   * @param camelContext in which endpoints are defined
   * @return {@link Endpoint}
   */
  public static Endpoint resolveEndpoint(Exchange exchange, CamelContext camelContext) {
    Route route = resolveRoute(exchange, camelContext);
    if (route == null) {
      throw SIPFrameworkException.init("Endpoint with id %s was not found", getRouteId(exchange));
    }
    return route.getEndpoint();
  }

  /**
   * Get camel route based on exchange route id
   *
   * @param exchange for fetching the route id
   * @param camelContext in which routes are defined
   * @return {@link Route}
   */
  public static Route resolveRoute(Exchange exchange, CamelContext camelContext) {
    return camelContext.getRoute(getRouteId(exchange));
  }

  /**
   * Get camel consumer based on exchange route id
   *
   * @param exchange for fetching the route id
   * @param camelContext in which consumers are defined
   * @return {@link Route}
   */
  public static Consumer resolveConsumer(Exchange exchange, CamelContext camelContext) {
    return resolveRoute(exchange, camelContext).getConsumer();
  }

  /**
   * Create exchange from test definition
   *
   * @param properties with route id and payload for exchange body
   * @param camelContext camel context
   * @return {@link Exchange}
   */
  public static Exchange parseExchangeProperties(
      EndpointProperties properties, CamelContext camelContext) {
    if (properties == null) {
      return anExchange(camelContext).build();
    }
    ExchangeBuilder exchangeBuilder =
        anExchange(camelContext)
            .withBody(getValueOrEval(properties.getRequestMessage().getEvaluatedBody()));
    properties
        .getRequestMessage()
        .getHeaders()
        .forEach(
            (key, value) -> {
              var evaluated = getValueOrEval(value);
              exchangeBuilder.withHeader(key, evaluated);
            });
    exchangeBuilder.withProperty(Mock.ENDPOINT_ID_EXCHANGE_PROPERTY, properties.getEndpointId());
    exchangeBuilder.withProperty(CONNECTOR_ID_EXCHANGE_PROPERTY, properties.getConnectorId());
    return exchangeBuilder.build();
  }

  public static Exchange parseAndEvaluateExchangeProperties(
      EndpointProperties properties, CamelContext camelContext, Context context) {
    if (properties == null) {
      return anExchange(camelContext).build();
    }
    ExchangeBuilder exchangeBuilder =
        anExchange(camelContext)
            .withBody(getValueOrEvaluateScript(properties.getRequestMessage().getEvaluatedBody(), context));
    properties
        .getRequestMessage()
        .getHeaders()
        .forEach(
            (key, value) -> {
              var evaluated = getValueOrEvaluateScript(value, context);
              exchangeBuilder.withHeader(key, evaluated);
            });
    exchangeBuilder.withProperty(Mock.ENDPOINT_ID_EXCHANGE_PROPERTY, properties.getEndpointId());
    exchangeBuilder.withProperty(CONNECTOR_ID_EXCHANGE_PROPERTY, properties.getConnectorId());
    return exchangeBuilder.build();
  }

  private static String getValueOrEval(PayloadProperties payloadProperties) {
    String jsScript = payloadProperties.getEval();
    if (StringUtils.isNotEmpty(jsScript)) {
      return EVAL_PREFIX + jsScript;
    }
    return payloadProperties.getValue();
  }

  private static String getValueOrEvaluateScript(PayloadProperties payloadProperties, Context context) {
    String jsScript = payloadProperties.getEval();
    if (StringUtils.isNotEmpty(jsScript)) {
      return evaluateScript(jsScript, context);
    }
    return payloadProperties.getValue();
  }

  public static String evaluateScript(String jsScript, Context context) {
      Value value = context.eval("js", jsScript);
      if (value == null || value.isNull()) {
        return null;
      }
      if (value.isBoolean()) {
        return String.valueOf(value.asBoolean());
      }
      if (value.isNumber()) {
        if (value.fitsInInt()) return String.valueOf(value.asInt());
        if (value.fitsInLong()) return String.valueOf(value.asLong());
        return String.valueOf(value.asDouble());
      }
      if (value.isString()) {
        return value.asString();
      }
      return value.toString();

//    } catch (PolyglotException e) {
//      throw SIPFrameworkException.init(
//          "Script threw an exception [%s]: %s", e.getMessage(), jsScript, e);
//    } catch (ClassCastException | UnsupportedOperationException e) {
//      throw SIPFrameworkException.init(
//          "Script returned an unexpected type [%s]: %s", e.getMessage(), jsScript, e);
//    }
  }

  public static ValidationResult evaluateValidationScript(String jsScript, String input, Context context) {

      if (StringUtils.isNotEmpty(input)) {
        context.getBindings("js").putMember("input", input);
      }
      Value value = context.eval("js", jsScript);
      if (value.isBoolean()) {
        return new ValidationResult(
            value.asBoolean(), "Validation script was evaluated as a boolean");
      }
      return new ValidationResult(true, value.asString());

  }

    public static Context createGraalJSContext() {
        return Context.newBuilder()
                .allowAllAccess(false)
                .option("engine.WarnInterpreterOnly", "false")
                .build();
    }

    /**
   * Checks if header is Test Kit specific header
   *
   * @param key of header for checking
   * @return boolean
   */
  public static boolean isTestKitHeader(String key) {
    return key.equals(TEST_NAME_HEADER) || key.equals(TEST_MODE_HEADER);
  }

  /**
   * Checks if header is Test Kit specific header
   *
   * @param inputExchange which body is converted from json to pojo
   * @param mapper for json unmarshalling
   * @param requestModelClass model of the pojo class
   */
  public static void unmarshallExchangeBodyFromJson(
      Exchange inputExchange, ObjectMapper mapper, Class<?> requestModelClass) {
    String jsonPayload = inputExchange.getMessage().getBody(String.class);
    try {
      inputExchange.getMessage().setBody(mapper.readValue(jsonPayload, requestModelClass));
    } catch (JsonProcessingException e) {
      throw new SIPFrameworkException(e);
    }
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
