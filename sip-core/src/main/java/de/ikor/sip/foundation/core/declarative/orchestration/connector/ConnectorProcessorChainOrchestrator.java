package de.ikor.sip.foundation.core.declarative.orchestration.connector;

import de.ikor.sip.foundation.core.declarative.annotation.connector.ConnectorRequestProcessor;
import de.ikor.sip.foundation.core.declarative.annotation.connector.ConnectorResponseProcessor;
import de.ikor.sip.foundation.core.declarative.connector.ConnectorDefinition;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.utils.DeclarativeReflectionUtils;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.RouteDefinition;
import org.springframework.context.ApplicationContext;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public final class ConnectorProcessorChainOrchestrator
    implements Orchestrator<ConnectorOrchestrationInfo> {

  final Supplier<ConnectorDefinition> relatedConnector;
  final Supplier<ApplicationContext> applicationContext;

  @Override
  public boolean canOrchestrate(ConnectorOrchestrationInfo info) {
    return info != null;
  }

  @Override
  public void doOrchestrate(ConnectorOrchestrationInfo info) {}

  private void buildRequestRouteProcessorChain(final RouteDefinition definition) {

    final var connector = relatedConnector.get();
    final var context = applicationContext.get();

    final var requestProcessorMethods =
        DeclarativeReflectionUtils.getAnnotatedMethods(
            connector.getClass(), ConnectorRequestProcessor.class);
    final var responseProcessorMethods =
        DeclarativeReflectionUtils.getAnnotatedMethods(
            connector.getClass(), ConnectorResponseProcessor.class);

    context.getBeansWithAnnotation(ConnectorRequestProcessor.class).values();
    context.getBeansWithAnnotation(ConnectorResponseProcessor.class).values();
  }
}
