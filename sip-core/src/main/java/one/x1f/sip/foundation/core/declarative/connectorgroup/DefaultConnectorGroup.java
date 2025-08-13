package one.x1f.sip.foundation.core.declarative.connectorgroup;

import lombok.AllArgsConstructor;

/**
 * Default connector group which is automatically created by framework when connector group is not
 * specified by user.
 *
 * <p><em>For internal use only.</em>
 */
@AllArgsConstructor
public final class DefaultConnectorGroup implements ConnectorGroupDefinition {

  private static final String EMPTY_PATH = "";

  private String connectorGroupId;

  @Override
  public String getId() {
    return connectorGroupId;
  }

  @Override
  public String getPathToDocumentationResource() {
    return EMPTY_PATH;
  }
}
