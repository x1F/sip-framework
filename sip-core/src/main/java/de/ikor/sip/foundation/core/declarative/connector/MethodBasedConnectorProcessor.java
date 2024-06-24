package de.ikor.sip.foundation.core.declarative.connector;

import de.ikor.sip.foundation.core.declarative.annotation.connector.HeaderParameter;
import de.ikor.sip.foundation.core.declarative.annotation.rest.PathParameter;
import de.ikor.sip.foundation.core.declarative.annotation.rest.QueryParameter;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.springframework.util.function.ThrowingFunction;

@ToString(onlyExplicitlyIncluded = true)
@Slf4j
public class MethodBasedConnectorProcessor implements ConnectorProcessor {

  @Getter @ToString.Include protected final ConnectorDefinition connector;
  @Getter @ToString.Include protected final Method processorMethod;
  @Getter protected final Set<Class<?>> requestedBodyTypes;
  @Getter protected final Class<?> returnType;
  protected final List<Function<Exchange, Object>> parameterFetchers;

  public MethodBasedConnectorProcessor(
      final ConnectorDefinition connector, final Method processorMethod) {
    this.connector = connector;
    this.processorMethod = processorMethod;
    this.returnType = processorMethod.getReturnType();
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
  public String getProcessorName() {
    return processorMethod.getName();
  }

  @Override
  public final void process(final Exchange exchange) throws SIPFrameworkException {
    try {
      final var args = parameterFetchers.stream().map(fetcher -> fetcher.apply(exchange)).toArray();
      final var result = processorMethod.invoke(connector, args);
      if (!Void.TYPE.equals(getReturnType())) {
        exchange.getMessage().setBody(result, getReturnType());
      }
    } catch (InvocationTargetException | IllegalAccessException e) {
      throw SIPFrameworkException.init(
          e,
          "Parameter-binding failed for method %s in connector-class %s",
          processorMethod.getName(),
          connector.getClass().getName());
    }
  }
}
