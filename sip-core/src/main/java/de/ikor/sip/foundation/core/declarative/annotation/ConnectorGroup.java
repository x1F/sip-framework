package de.ikor.sip.foundation.core.declarative.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * Annotation to be used with a class extending {@link
 * de.ikor.sip.foundation.core.declarative.connectorgroup.ConnectorGroupBase}.
 *
 * <p>This annotation allows to easily provide information for (some) of the data required by the
 * {@link de.ikor.sip.foundation.core.declarative.connectorgroup.ConnectorGroupDefinition}
 * interface.
 *
 * @see de.ikor.sip.foundation.core.declarative.connectorgroup.ConnectorGroupBase
 * @see de.ikor.sip.foundation.core.declarative.connectorgroup.ConnectorGroupDefinition
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface ConnectorGroup {

  /**
   * @return ID of the connector group. Must be unique within the scope of the adapter.
   * @see de.ikor.sip.foundation.core.declarative.connectorgroup.ConnectorGroupDefinition#getId()
   */
  String groupId();

  /**
   * Optional path to the resource (typically a markdown file) that describes this connector group.
   * If not given, an attempt is made to retrieve documentation from <code>
   * document/structure/connector-groups/&lt;connector-group-ID&gt;.md</code> automatically.
   *
   * @return Optional path to documentation resource file
   */
  String pathToDocumentationResource() default "";
}
