package de.ikor.sip.foundation.core.declarative.model;

import de.ikor.sip.foundation.core.declarative.connector.ConnectorExtension;
import de.ikor.sip.foundation.core.declarative.connector.ConnectorProcessor;
import de.ikor.sip.foundation.core.declarative.utils.DeclarativeHelper;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkException;
import org.apache.camel.Exchange;

/**
 * Interface for mappers between two data types
 *
 * @param <S> Source Type
 * @param <T> Target Type
 */
@SuppressWarnings("unchecked")
@FunctionalInterface
public interface ModelMapper<S, T> extends ConnectorProcessor {

  String MAPPING_METHOD_NAME = "mapToTargetModel";

  /**
   * Maps the given <code>sourceModel</code> to the target type
   *
   * @param sourceModel Element to map
   * @return Element mapped to target type
   */
  T mapToTargetModel(S sourceModel);

  /**
   * Maps the given <code>sourceModel</code> to the target type.
   *
   * <p>The default implementation simply forwards the mapping to {@link #mapToTargetModel(Object)}
   * and omits the given <code>excchange</code>.
   *
   * @param sourceModel Element to map
   * @param exchange Current exchange
   * @return Element mapped to target type
   */
  default T mapToTargetModel(S sourceModel, Exchange exchange) {
    return mapToTargetModel(sourceModel);
  }

  /**
   * Derives the source-class from the generic type
   *
   * @return Source class
   */
  default Class<S> getSourceModelClass() {
    return (Class<S>) DeclarativeHelper.getMappingMethod(getClass()).getParameterTypes()[0];
  }

  /**
   * Derives the target-class from the generic type
   *
   * @return Target class
   */
  default Class<T> getTargetModelClass() {
    return (Class<T>) DeclarativeHelper.getMappingMethod(getClass()).getReturnType();
  }

  /**
   * Maps from source to target model using the {@link ConnectorExtension} API.
   *
   * <p>The default implementation uses {@link #mapToTargetModel(Object, Exchange)} to map the
   * current message body to the target type.
   *
   * @see ConnectorProcessor
   * @param exchange Current message exchange
   * @throws SIPFrameworkException Mapping failure
   */
  @Override
  default void process(Exchange exchange) throws SIPFrameworkException {
    try {
      final var bodyUnmapped = exchange.getMessage().getMandatoryBody(getSourceModelClass());
      final var bodyMapped = mapToTargetModel(bodyUnmapped, exchange);
      exchange.getMessage().setBody(bodyMapped, getTargetModelClass());
    } catch (Exception e) {
      throw SIPFrameworkException.init(
          e,
          "Failed to apply model-mapper implementation in class %s from source-type %s to target-type %s",
          getClass().getName(),
          getSourceModelClass().getSimpleName(),
          getTargetModelClass().getSimpleName());
    }
  }
}
