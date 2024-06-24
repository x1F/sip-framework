package de.ikor.sip.foundation.core.declarative;

import static java.util.Map.entry;
import static java.util.function.Predicate.not;

import de.ikor.sip.foundation.core.declarative.annonation.*;
import de.ikor.sip.foundation.core.declarative.connector.*;
import de.ikor.sip.foundation.core.declarative.connectorgroup.ConnectorGroupBase;
import de.ikor.sip.foundation.core.declarative.connectorgroup.ConnectorGroupDefinition;
import de.ikor.sip.foundation.core.declarative.connectorgroup.DefaultConnectorGroup;
import de.ikor.sip.foundation.core.declarative.process.CompositeProcessBase;
import de.ikor.sip.foundation.core.declarative.process.CompositeProcessDefinition;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioConsumerDefinition;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioProviderDefinition;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Getter
@Service
@Slf4j
public final class DeclarationsRegistry implements DeclarationsRegistryApi {

  private final ApplicationContext applicationContext;
  private final List<ConnectorGroupDefinition> connectorGroups;
  private final List<CompositeProcessDefinition> processes;
  private final List<IntegrationScenarioDefinition> scenarios;
  private final List<ConnectorDefinition> connectors;

  public DeclarationsRegistry(
      List<ConnectorGroupDefinition> autowiredConnectorGroups,
      List<IntegrationScenarioDefinition> autowiredScenarios,
      List<ConnectorDefinition> autowiredConnectors,
      List<CompositeProcessDefinition> compositeProcessDefinitions,
      ApplicationContext applicationContext) {

    this.applicationContext = applicationContext;
    this.connectorGroups =
        autowiredConnectorGroups.stream().filter(not(isDisabled())).collect(Collectors.toList());

    this.scenarios = autowiredScenarios.stream().filter(not(isDisabled())).toList();

    this.connectors =
        autowiredConnectors.stream()
            .filter(not(isDisabled(autowiredScenarios, autowiredConnectorGroups)))
            .toList();

    this.processes = compositeProcessDefinitions.stream().filter(not(isDisabled())).toList();

    createMissingConnectorGroups();
    checkForDuplicateDeclarativeElements();
    checkAnnotatedClassForMissingParents();
    checkForUnusedScenarios();
  }

  private void checkAnnotatedClassForMissingParents() {
    Map.ofEntries(
            entry(IntegrationScenario.class, IntegrationScenarioBase.class),
            entry(ConnectorGroup.class, ConnectorGroupBase.class),
            entry(InboundConnector.class, InboundConnectorBase.class),
            entry(OutboundConnector.class, GenericOutboundConnectorBase.class),
            entry(CompositeProcess.class, CompositeProcessBase.class))
        .forEach(this::checkAnnotatedClassForMissingParent);
  }

  private void checkAnnotatedClassForMissingParent(
      Class<? extends Annotation> annotatedClass, Class<?> parentClass) {
    applicationContext
        .getBeansWithAnnotation(annotatedClass)
        .values()
        .forEach(
            o -> {
              if (!parentClass.isInstance(o)) {
                throw SIPFrameworkInitializationException.init(
                    "Annotated %s %s is not inheriting %s parent class or any of it's child classes. Please inherit the proper class.",
                    annotatedClass.getSimpleName(),
                    o.getClass().getName(),
                    parentClass.getSimpleName());
              }
            });
  }

  private void createMissingConnectorGroups() {
    connectors.stream()
        .filter(connector -> getConnectorGroupById(connector.getConnectorGroupId()).isEmpty())
        .forEach(
            connector ->
                connectorGroups.add(new DefaultConnectorGroup(connector.getConnectorGroupId())));
  }

  private Optional<ConnectorGroupDefinition> getConnectorGroupById(final String connectorGroupId) {
    return connectorGroups.stream()
        .filter(connector -> connector.getId().equals(connectorGroupId))
        .findFirst();
  }

  private void checkForDuplicateDeclarativeElements() {
    checkForDuplicatesAndThrowError(scenarios, "integration scenario");
    checkForDuplicatesAndThrowError(connectors, "connector");
    checkForDuplicatesAndThrowError(processes, "composite process");
    checkForDuplicatesAndThrowError(connectorGroups, "connector group");
  }

  private <T extends DeclarativeElement> void checkForDuplicatesAndThrowError(
      List<T> elements, String elementType) {
    Set<String> set = new HashSet<>();
    elements.stream()
        .filter(n -> !set.add(n.getId()))
        .forEach(
            element -> {
              throw SIPFrameworkInitializationException.init(
                  "There is a non-unique %s ID '%s' in class %s. A unique ID should be provided in the element's annotation.",
                  elementType, element.getId(), element.getClass().getName());
            });
  }

  private void checkForUnusedScenarios() {
    scenarios.stream()
        .filter(scenario -> getProvidersForScenario(scenario).isEmpty())
        .forEach(
            scenario -> {
              throw SIPFrameworkInitializationException.init(
                  "Nothing is providing data to the integration scenario with id '%s'",
                  scenario.getId());
            });
    scenarios.stream()
        .filter(scenario -> getConsumersForScenario(scenario).isEmpty())
        .forEach(
            scenario -> {
              throw SIPFrameworkInitializationException.init(
                  "Nothing is consuming data from the integration scenario with id '%s'",
                  scenario.getId());
            });
  }

  @Override
  public IntegrationScenarioDefinition getScenarioById(final String scenarioId) {
    return scenarios.stream()
        .filter(scenario -> scenario.getId().equals(scenarioId))
        .findFirst()
        .orElseThrow(
            () ->
                SIPFrameworkInitializationException.init(
                    "There is no integration scenario with id: %s", scenarioId));
  }

  @Override
  public Optional<ConnectorDefinition> getConnectorById(final String connectorId) {
    return connectors.stream()
        .filter(connector -> connector.getId().equals(connectorId))
        .findFirst();
  }

  @SuppressWarnings("rawtypes")
  @Override
  public List<InboundConnectorDefinition> getInboundConnectors() {
    return connectors.stream()
        .filter(InboundConnectorDefinition.class::isInstance)
        .map(InboundConnectorDefinition.class::cast)
        .toList();
  }

  @Override
  public List<OutboundConnectorDefinition> getOutboundConnectors() {
    return connectors.stream()
        .filter(OutboundConnectorDefinition.class::isInstance)
        .map(OutboundConnectorDefinition.class::cast)
        .toList();
  }

  @SuppressWarnings("rawtypes")
  @Override
  public List<InboundConnectorDefinition> getInboundConnectorsByScenarioId(String scenarioId) {
    return connectors.stream()
        .filter(connector -> connector.getScenarioId().equals(scenarioId))
        .filter(InboundConnectorDefinition.class::isInstance)
        .map(InboundConnectorDefinition.class::cast)
        .toList();
  }

  @Override
  public List<OutboundConnectorDefinition> getOutboundConnectorsByScenarioId(String scenarioId) {
    return connectors.stream()
        .filter(connector -> connector.getScenarioId().equals(scenarioId))
        .filter(OutboundConnectorDefinition.class::isInstance)
        .map(OutboundConnectorDefinition.class::cast)
        .toList();
  }

  @Override
  public List<IntegrationScenarioDefinition> getCompositeProcessConsumerDefinitions(
      String compositeProcessID) {
    return getProcessById(compositeProcessID).getConsumerDefinitions().stream()
        .map(definition -> (IntegrationScenarioDefinition) applicationContext.getBean(definition))
        .toList();
  }

  @Override
  public IntegrationScenarioDefinition getCompositeProcessProviderDefinition(
      String compositeProcessID) {
    return applicationContext.getBean(getProcessById(compositeProcessID).getProviderDefinition());
  }

  private CompositeProcessDefinition getProcessById(String compositeProcessID) {
    return processes.stream()
        .filter(process -> process.getId().equals(compositeProcessID))
        .findFirst()
        .orElseThrow(
            () ->
                SIPFrameworkInitializationException.init(
                    "Composite process '%s' can not be found in the registry. Please check your configuration",
                    compositeProcessID));
  }

  @Override
  public IntegrationScenarioBase getIntegrationScenarioBase(String compositeProcessID) {
    return (IntegrationScenarioBase) getCompositeProcessProviderDefinition(compositeProcessID);
  }

  @Override
  public List<CompositeProcessDefinition> getCompositeProcessProvidersForScenario(
      IntegrationScenarioDefinition integrationScenario) {
    return processes.stream()
        .filter(
            composite ->
                composite.getConsumerDefinitions().stream()
                    .anyMatch(consumer -> consumer.equals(integrationScenario.getClass())))
        .toList();
  }

  @Override
  public List<CompositeProcessDefinition> getCompositeProcessConsumersForScenario(
      IntegrationScenarioDefinition integrationScenario) {
    return processes.stream()
        .filter(
            composite ->
                Stream.of(composite.getProviderDefinition())
                    .anyMatch(consumer -> consumer.equals(integrationScenario.getClass())))
        .toList();
  }

  @Override
  public List<IntegrationScenarioProviderDefinition> getProvidersForScenario(
      IntegrationScenarioDefinition integrationScenario) {
    List<IntegrationScenarioProviderDefinition> inboundConnectorsForScenario =
        List.copyOf(getInboundConnectorsByScenarioId(integrationScenario.getId()));
    List<IntegrationScenarioProviderDefinition> compositeProcessProvidersForScenario =
        List.copyOf(getCompositeProcessProvidersForScenario(integrationScenario));
    return Stream.concat(
            inboundConnectorsForScenario.stream(), compositeProcessProvidersForScenario.stream())
        .toList();
  }

  public List<IntegrationScenarioConsumerDefinition> getConsumersForScenario(
      IntegrationScenarioDefinition integrationScenario) {
    List<IntegrationScenarioConsumerDefinition> outboundConnectorsForScenario =
        List.copyOf(getOutboundConnectorsByScenarioId(integrationScenario.getId()));
    List<IntegrationScenarioConsumerDefinition> compositeProcessConsumersForScenario =
        List.copyOf(getCompositeProcessConsumersForScenario(integrationScenario));
    return Stream.concat(
            outboundConnectorsForScenario.stream(), compositeProcessConsumersForScenario.stream())
        .toList();
  }

  private Predicate<ConnectorDefinition> isDisabled(
      List<IntegrationScenarioDefinition> scenarios,
      List<ConnectorGroupDefinition> connectorGroups) {
    return connector -> {
      if (isDisabled().test(connector)) {
        return true;
      }

      Optional<IntegrationScenarioDefinition> scenarioDefinition =
          scenarios.stream()
              .filter(scenario -> scenario.getId().equals(connector.getScenarioId()))
              .findFirst();
      if (scenarioDefinition.isPresent() && isDisabled().test(scenarioDefinition.get())) {
        return true;
      }

      Optional<ConnectorGroupDefinition> connectorGroupDefinition =
          connectorGroups.stream()
              .filter(group -> group.getId().equals(connector.getConnectorGroupId()))
              .findFirst();
      return connectorGroupDefinition.isPresent()
          && isDisabled().test(connectorGroupDefinition.get());
    };
  }

  private Predicate<Object> isDisabled() {
    return elem -> elem.getClass().isAnnotationPresent(Disabled.class);
  }
}
