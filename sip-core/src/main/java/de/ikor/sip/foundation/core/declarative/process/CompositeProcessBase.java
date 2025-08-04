package de.ikor.sip.foundation.core.declarative.process;

import de.ikor.sip.foundation.core.declarative.annotation.CompositeProcess;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import de.ikor.sip.foundation.core.declarative.utils.DeclarativeReflectionUtils;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import java.util.List;

/**
 * Base class for a composite process definition.
 *
 * <p>Adapter developers should extend this class and annotate it with @{@link CompositeProcess}.
 *
 * @see CompositeProcess
 */
public class CompositeProcessBase implements CompositeProcessDefinition {

  public Orchestrator<CompositeProcessOrchestrationInfo> getOrchestrator() {
    throw SIPFrameworkInitializationException.init(
        "Orchestration needs to be defined for the process '%s' declared in the class '%s'. Please @Override the getOrchestrator() method.",
        getId(), getClass().getName());
  }

  private final CompositeProcess processAnnotation =
      DeclarativeReflectionUtils.getAnnotationOrThrow(CompositeProcess.class, this);

  public final String getId() {
    return processAnnotation.processId();
  }

  public List<Class<? extends IntegrationScenarioDefinition>> getConsumerDefinitions() {
    return List.of(processAnnotation.consumers());
  }

  public Class<? extends IntegrationScenarioDefinition> getProviderDefinition() {
    return processAnnotation.provider();
  }

  public String getPathToDocumentationResource() {
    return processAnnotation.pathToDocumentationResource();
  }
}
