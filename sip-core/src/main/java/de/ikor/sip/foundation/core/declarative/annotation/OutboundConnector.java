package de.ikor.sip.foundation.core.declarative.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * Annotation to be used with an outbound connector base class (such as {@link
 * de.ikor.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase}).
 *
 * <p>This annotation allows to easily provide information for (some) of the data required by the
 * {@link de.ikor.sip.foundation.core.declarative.connector.OutboundConnectorDefinition} interface.
 *
 * @see de.ikor.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase
 * @see de.ikor.sip.foundation.core.declarative.connector.OutboundConnectorDefinition
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface OutboundConnector {

  /**
   * @return Identifier of the connector. Must be unique within the scope of the adapter.
   * @see de.ikor.sip.foundation.core.declarative.connector.OutboundConnectorDefinition#getId()
   */
  String connectorId() default "";

  /**
   * @return Identifier of the connector group this connector belongs to.
   * @see
   *     de.ikor.sip.foundation.core.declarative.connector.OutboundConnectorDefinition#getConnectorGroupId()
   */
  String connectorGroup();

  /**
   * @return Identifier of the integration scenario that the connector is consuming from.
   */
  String integrationScenario();

  /**
   * @return Request model base class for this connector
   * @see
   *     de.ikor.sip.foundation.core.declarative.connector.OutboundConnectorDefinition#getRequestModelClass()
   */
  Class<?> requestModel();

  /**
   * @return Optional response model base class for this connector
   * @see
   *     de.ikor.sip.foundation.core.declarative.connector.OutboundConnectorDefinition#getResponseModelClass()
   */
  Class<?> responseModel() default Void.class;

  /**
   * @return Optional domains this connector is a part of (such as functional domains or external
   *     systems)
   */
  String[] domains() default {};

  /**
   * Optional path to the resource (typically a markdown file) that describes this connector. If not
   * given, an attempt is made to retrieve documentation from <code>
   * document/structure/connectors/&lt;connector-ID&gt;.md</code> automatically.
   *
   * @return Optional path to documentation resource file
   */
  String pathToDocumentationResource() default "";
}
