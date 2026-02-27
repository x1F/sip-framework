package one.x1f.sip.foundation.connectors.con1;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import one.x1f.sip.foundation.core.declarative.ConnectorRegistry;
import one.x1f.sip.foundation.core.declarative.connector.OutboundConnectorDefinition;
import one.x1f.sip.foundation.core.declarative.orchestration.Orchestrator;
import one.x1f.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import org.apache.camel.model.RouteDefinition;

public abstract class NoAnnotationAbstractConnector implements OutboundConnectorDefinition {

  @Override
  public void defineOutboundEndpoints(
      RouteDefinition routeDefinition, ConnectorRegistry connectorRegistry) {
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
