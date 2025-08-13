package one.x1f.sip.foundation.core.declarative.orchestration.process.routebuilding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import one.x1f.sip.foundation.core.apps.declarative.ProcessOrchestrationConditionalAdapter;
import one.x1f.sip.foundation.core.declarative.DeclarationsRegistry;
import one.x1f.sip.foundation.core.declarative.DeclarationsRegistryApi;
import one.x1f.sip.foundation.core.declarative.RoutesRegistry;
import one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationInfo;
import one.x1f.sip.foundation.core.declarative.orchestration.process.dsl.CallNestedCondition;
import one.x1f.sip.foundation.core.declarative.orchestration.process.dsl.DSLTestHelper;
import one.x1f.sip.foundation.core.declarative.orchestration.process.dsl.ProcessOrchestrationDefinition;
import one.x1f.sip.foundation.core.declarative.orchestration.process.dsl.RouteGeneratorInternalHelper;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import org.apache.camel.model.RoutesDefinition;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class RouteGeneratorForProcessOrchestrationDefinitionTest {

  @Test
  void When_OrchestrationProviderMissing_Expect_SIPFrameworkInitializationException() {
    // arrange
    CompositeProcessOrchestrationInfo compositeProcessOrchestrationInfo =
        mock(CompositeProcessOrchestrationInfo.class, RETURNS_DEEP_STUBS);
    RouteGeneratorForProcessOrchestrationDefinition subject =
        new RouteGeneratorForProcessOrchestrationDefinition(
            compositeProcessOrchestrationInfo, null);
    RoutesDefinition routesDefinition = mock(RoutesDefinition.class, RETURNS_DEEP_STUBS);
    when(compositeProcessOrchestrationInfo.getRoutesDefinition()).thenReturn(routesDefinition);
    when(routesDefinition
            .getCamelContext()
            .getRegistry()
            .findSingleByType(DeclarationsRegistryApi.class))
        .thenReturn(mock(DeclarationsRegistry.class));
    when(compositeProcessOrchestrationInfo.getCompositeProcess().getId()).thenReturn("test");

    // act + assert
    assertThatThrownBy(
            () -> {
              subject.generateRoutes(routesDefinition);
            })
        .isInstanceOf(SIPFrameworkInitializationException.class)
        .hasMessage(
            "Orchestration for composite process 'test' doesn't have a provider. Please define it.");
  }

  @Test
  void when_NoBranchStatements_expect_LogWarning() {
    // arrange
    Logger logger =
        (Logger) LoggerFactory.getLogger(RouteGeneratorForProcessOrchestrationDefinition.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.setLevel(Level.DEBUG);
    logger.addAppender(listAppender);
    List<ILoggingEvent> logsList = listAppender.list;

    CompositeProcessOrchestrationInfo compositeProcessOrchestrationInfo =
        mock(CompositeProcessOrchestrationInfo.class, RETURNS_DEEP_STUBS);
    CallNestedCondition callNestedCondition = DSLTestHelper.initCallNestedCondition();
    RouteGeneratorInternalHelper.getConditionalStatements(callNestedCondition)
        .add(new CallNestedCondition.ProcessBranchStatements(null, new ArrayList<>()));
    when(compositeProcessOrchestrationInfo.getCompositeProcess().getId())
        .thenReturn("ID of process");
    RoutesDefinition routesDefinition = mock(RoutesDefinition.class, RETURNS_DEEP_STUBS);
    when(compositeProcessOrchestrationInfo.getRoutesDefinition()).thenReturn(routesDefinition);
    DeclarationsRegistry declarationsRegistry = mock(DeclarationsRegistry.class);
    when(routesDefinition
            .getCamelContext()
            .getRegistry()
            .findSingleByType(DeclarationsRegistryApi.class))
        .thenReturn(declarationsRegistry);
    when(routesDefinition.getCamelContext().getRegistry().findSingleByType(RoutesRegistry.class))
        .thenReturn(mock(RoutesRegistry.class));
    when(declarationsRegistry.getCompositeProcessProviderDefinition(any()))
        .thenReturn(mock(IntegrationScenarioDefinition.class));
    when(compositeProcessOrchestrationInfo.getConsumerEndpoints().keySet())
        .thenReturn(Set.of(mock(ProcessOrchestrationConditionalAdapter.LoggingScenario.class)));
    RouteGeneratorForProcessOrchestrationDefinition subject =
        new RouteGeneratorForProcessOrchestrationDefinition(
            compositeProcessOrchestrationInfo,
            mock(ProcessOrchestrationDefinition.class, RETURNS_DEEP_STUBS));

    // act
    subject.generateRoutes(routesDefinition);
    ILoggingEvent log = logsList.get(0);

    // assert
    assertThat(log.getMessage())
        .contains(
            "Orchestration for composite process '{}' does not call consumers '{}' for calls coming in from '{}'. Consider removing the consumer from the process definition.");
    assertThat(log.getLevel()).isEqualTo(Level.WARN);
  }
}
