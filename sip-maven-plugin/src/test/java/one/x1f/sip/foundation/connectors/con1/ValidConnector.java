package one.x1f.sip.foundation.connectors.con1;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import one.x1f.sip.foundation.core.declarative.ConnectorRegistry;
import one.x1f.sip.foundation.core.declarative.DeclarationsRegistry;
import one.x1f.sip.foundation.core.declarative.RoutesRegistry;
import one.x1f.sip.foundation.core.declarative.annotation.InboundConnector;
import one.x1f.sip.foundation.core.declarative.connector.InboundConnectorDefinition;
import one.x1f.sip.foundation.core.declarative.orchestration.Orchestrator;
import one.x1f.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import org.apache.camel.model.OptionalIdentifiedDefinition;

@InboundConnector(
    connectorGroup = "test",
    integrationScenario = "test",
    requestModel = Object.class)
public class ValidConnector implements InboundConnectorDefinition {
  @Override
  public void defineInboundEndpoints(
      OptionalIdentifiedDefinition definition,
      String targetToBase,
      RoutesRegistry routeRegistry,
      DeclarationsRegistry declarationsRegistry,
      ConnectorRegistry connectorRegistry) {
    // test
  }

  @Override
  public String getConnectorGroupId() {
    return "";
  }

  @Override
  public String getScenarioId() {
    return "";
  }

  @Override
  public Class<?> getRequestModelClass() {
    return null;
  }

  @Override
  public Optional<Class<?>> getResponseModelClass() {
    return Optional.empty();
  }

  @Override
  public String[] getConfigurationIds() {
    return new String[0];
  }

  @Override
  public List<Method> getOnExceptionHandler() {
    return List.of();
  }

  @Override
  public Class getEndpointDefinitionTypeClass() {
    return null;
  }

  @Override
  public String getId() {
    return "";
  }

  @Override
  public String getPathToDocumentationResource() {
    return "";
  }

  @Override
  public Orchestrator<ConnectorOrchestrationInfo> getOrchestrator() {
    return null;
  }
}
