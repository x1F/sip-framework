package one.x1f.sip.foundation.core.declarative.connector;

import jakarta.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.core.declarative.annotation.connector.extension.HeaderParameter;
import one.x1f.sip.foundation.core.declarative.annotation.rest.PathParameter;
import one.x1f.sip.foundation.core.declarative.annotation.rest.QueryParameter;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.springframework.util.function.ThrowingFunction;

@ToString(onlyExplicitlyIncluded = true)
@Slf4j
public class MethodBasedConnectorProcessor implements ConnectorProcessor {

  @Getter @ToString.Include protected final ConnectorDefinition connector;
  @Getter @ToString.Include protected final Method processorMethod;
  @Getter protected final Set<Class<?>> requestedBodyTypes;
  @Getter protected final Optional<Class<?>> returnType;
  protected final List<Function<Exchange, Object>> parameterFetchers;

  public MethodBasedConnectorProcessor(
      final ConnectorDefinition connector, final Method processorMethod) {
    this.connector = connector;
    this.processorMethod = processorMethod;
    this.returnType =
        !Void.TYPE.equals(processorMethod.getReturnType())
            ? Optional.of(processorMethod.getReturnType())
            : Optional.empty();
    Set<Class<?>> bodyTypes = new HashSet<>();
    parameterFetchers =
        Arrays.stream(processorMethod.getParameters())
            .map(param -> getFetcherForParameter(param, bodyTypes))
            .toList();
    this.requestedBodyTypes = Collections.unmodifiableSet(bodyTypes);
  }

  protected Function<Exchange, Object> getFetcherForParameter(
      final Parameter parameter, final Set<Class<?>> bodyTypes) {
    final var type = parameter.getType();
    if (Exchange.class.isAssignableFrom(type)) {
      return exchange -> exchange;
    } else if (Message.class.isAssignableFrom(type)) {
      return Exchange::getMessage;
    } else if (parameter.isAnnotationPresent(HeaderParameter.class)) {
      final var headerName = parameter.getAnnotation(HeaderParameter.class).value();
      return exchange -> exchange.getMessage().getHeader(headerName, type);
    } else if (parameter.isAnnotationPresent(PathParameter.class)) {
      final var headerName = parameter.getAnnotation(PathParameter.class).value();
      return exchange -> exchange.getMessage().getHeader(headerName, type);
    } else if (parameter.isAnnotationPresent(QueryParameter.class)) {
      final var headerName = parameter.getAnnotation(QueryParameter.class).value();
      return exchange -> exchange.getMessage().getHeader(headerName, type);
    } else {
      bodyTypes.add(type);
      if (parameter.isAnnotationPresent(Nullable.class)) {
        return exchange -> exchange.getMessage().getBody(type);
      } else {
        return ThrowingFunction.of(exchange -> exchange.getMessage().getMandatoryBody(type));
      }
    }
  }

  @Override
  public String getExtensionName() {
    return processorMethod.getName();
  }

  @Override
  public final void process(final Exchange exchange) throws Exception {
    try {
      final var args = parameterFetchers.stream().map(fetcher -> fetcher.apply(exchange)).toArray();
      final var result = processorMethod.invoke(connector, args);
      getReturnType().ifPresent(type -> exchange.getMessage().setBody(result, type));
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof Exception exception) throw exception;
      throw SIPFrameworkException.init(
          e.getCause(),
          "An error occurred while running connector processor method '%s' in connector-class %s: %s",
          processorMethod.getName(),
          connector.getClass().getName(),
          e.getCause().getMessage());
    } catch (IllegalAccessException e) {
      throw SIPFrameworkException.init(
          e,
          "Failed to run connector processor method '%s' in connector-class %s",
          processorMethod.getName(),
          connector.getClass().getName());
    }
  }
}
