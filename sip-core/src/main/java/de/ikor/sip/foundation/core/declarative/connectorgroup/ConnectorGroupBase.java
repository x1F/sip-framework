package de.ikor.sip.foundation.core.declarative.connectorgroup;

import de.ikor.sip.foundation.core.declarative.annotation.ConnectorGroup;
import de.ikor.sip.foundation.core.declarative.connector.ConnectorDefinition;
import de.ikor.sip.foundation.core.declarative.utils.DeclarativeReflectionUtils;

/**
 * Base class for a connector group definition.
 *
 * <p>Specifying a connector group with this base class is only necessary in special cases, as
 * connector groups are created automatically if no explicit implementation is found for an
 * identifier that is specified in a connector via {@link
 * ConnectorDefinition#getConnectorGroupId()}.
 *
 * <p>Adapter developers should extend this class and annotate it with @{@link ConnectorGroup}.
 */
public class ConnectorGroupBase implements ConnectorGroupDefinition {

  private final ConnectorGroup annotation =
      DeclarativeReflectionUtils.getAnnotationOrThrow(ConnectorGroup.class, this);

  @Override
  public final String getId() {
    return annotation.groupId();
  }

  @Override
  public String getPathToDocumentationResource() {
    return annotation.pathToDocumentationResource();
  }
}
