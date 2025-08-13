package one.x1f.sip.foundation.core.declarative.orchestration.process.routebuilding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationInfo;
import one.x1f.sip.foundation.core.declarative.orchestration.process.dsl.CallNestedCondition;
import one.x1f.sip.foundation.core.declarative.orchestration.process.dsl.DSLTestHelper;
import one.x1f.sip.foundation.core.declarative.orchestration.process.dsl.RouteGeneratorInternalHelper;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.RouteDefinition;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class RouteGeneratorForCallConditionalProcessConsumerTest {

  @Test
  void when_NoStatements_expect_InitException() {
    // arrange
    CompositeProcessOrchestrationInfo compositeProcessOrchestrationInfo =
        mock(CompositeProcessOrchestrationInfo.class, RETURNS_DEEP_STUBS);
    CallNestedCondition callNestedCondition = mock(CallNestedCondition.class);
    when(compositeProcessOrchestrationInfo.getCompositeProcess().getId())
        .thenReturn("ID of process");
    RouteGeneratorForCallConditionalProcessConsumer subject =
        new RouteGeneratorForCallConditionalProcessConsumer(
            compositeProcessOrchestrationInfo, callNestedCondition, new HashSet<>());

    // act + assert
    assertThatThrownBy(() -> subject.generateRoute(mock(RouteDefinition.class)))
        .isInstanceOf(SIPFrameworkInitializationException.class);
  }

  @Test
  void when_NoBranchStatements_expect_LogWarning() {
    // arrange
    Logger logger =
        (Logger) LoggerFactory.getLogger(RouteGeneratorForCallConditionalProcessConsumer.class);
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
    RouteDefinition routeDefinition = mock(RouteDefinition.class);
    when(routeDefinition.choice()).thenReturn(mock(ChoiceDefinition.class));
    RouteGeneratorForCallConditionalProcessConsumer generator =
        new RouteGeneratorForCallConditionalProcessConsumer(
            compositeProcessOrchestrationInfo, callNestedCondition, new HashSet<>());

    // act
    generator.generateRoute(routeDefinition);
    ILoggingEvent subject = logsList.get(0);

    // assert
    assertThat(subject.getFormattedMessage())
        .contains(
            "Orchestration for composite process ID of process contains a conditional-statement that does not specify any actions in branch");
    assertThat(subject.getLevel()).isEqualTo(Level.WARN);
  }
}
