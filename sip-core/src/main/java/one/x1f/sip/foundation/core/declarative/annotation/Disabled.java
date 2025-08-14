package one.x1f.sip.foundation.core.declarative.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import one.x1f.sip.foundation.core.declarative.connector.ConnectorDefinition;
import one.x1f.sip.foundation.core.declarative.connectorgroup.ConnectorGroupDefinition;
import one.x1f.sip.foundation.core.declarative.process.CompositeProcessDefinition;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;

/**
 * Annotation that can be used to disable:
 *
 * <ul>
 *   <li>{@link ConnectorDefinition}s
 *   <li>{@link IntegrationScenarioDefinition}s, which also disables all consumers and providers
 *       attached to the scenario
 *   <li>{@link ConnectorGroupDefinition}s, which also disables all connectors attached to it
 *   <li>{@link CompositeProcessDefinition}s
 * </ul>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Disabled {}
