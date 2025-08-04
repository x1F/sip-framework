package de.ikor.sip.foundation.core.declarative.connector;

import static de.ikor.sip.foundation.core.declarative.utils.DeclarativeHelper.formatConnectorId;

import de.ikor.sip.foundation.core.declarative.annotation.InboundConnector;
import de.ikor.sip.foundation.core.declarative.utils.DeclarativeReflectionUtils;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * Base class for Inbound connector definitions
 *
 * <p>This class provides a default implementation that reads the values provided in the {@link
 * InboundConnector} annotation. It's meant to be extended by different types of inbound connectors.
 *
 * @see GenericInboundConnectorBase
 * @see RestInboundConnectorBase
 */
public abstract class InboundConnectorBase extends ConnectorBase {

  private final InboundConnector inboundConnectorAnnotation =
      DeclarativeReflectionUtils.getAnnotationOrThrow(InboundConnector.class, this);

  private final String connectorId =
      StringUtils.defaultIfEmpty(
          inboundConnectorAnnotation.connectorId(),
          formatConnectorId(getConnectorType(), getScenarioId(), getConnectorGroupId()));

  @Override
  public final String getId() {
    return connectorId;
  }

  @Override
  public final String getConnectorGroupId() {
    return inboundConnectorAnnotation.connectorGroup();
  }

  @Override
  public final Class<?> getRequestModelClass() {
    return inboundConnectorAnnotation.requestModel();
  }

  @Override
  public final Optional<Class<?>> getResponseModelClass() {
    var clazz = inboundConnectorAnnotation.responseModel();
    return clazz.equals(Void.class) ? Optional.empty() : Optional.of(clazz);
  }

  @Override
  public String getPathToDocumentationResource() {
    return inboundConnectorAnnotation.pathToDocumentationResource();
  }

  @Override
  public String getScenarioId() {
    return inboundConnectorAnnotation.integrationScenario();
  }
}
