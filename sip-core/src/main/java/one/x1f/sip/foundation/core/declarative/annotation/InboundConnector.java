package one.x1f.sip.foundation.core.declarative.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import one.x1f.sip.foundation.core.declarative.connector.GenericInboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.connector.InboundConnectorDefinition;
import org.springframework.stereotype.Component;

/**
 * Annotation to be used with an inbound connector base class (such as {@link
 * GenericInboundConnectorBase}).
 *
 * <p>This annotation allows to easily provide information for (some) of the data required by the
 * {@link InboundConnectorDefinition} interface.
 *
 * @see GenericInboundConnectorBase
 * @see InboundConnectorDefinition
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface InboundConnector {

  /**
   * @return Identifier of the connector. Must be unique within the scope of the adapter.
   * @see InboundConnectorDefinition#getId()
   */
  String connectorId() default "";

  /**
   * @return Identifier of the connector group this connector belongs to.
   * @see InboundConnectorDefinition#getConnectorGroupId()
   */
  String connectorGroup();

  /** return Identifier of the integration scenario that the connector is providing to. */
  String integrationScenario();

  /**
   * @return Request model base class for this connector
   * @see InboundConnectorDefinition#getRequestModelClass()
   */
  Class<?> requestModel();

  /**
   * @return Response model base class for this connector
   * @see InboundConnectorDefinition#getResponseModelClass()
   */
  Class<?> responseModel() default Void.class;

  /**
   * @return Domains this connector is a part of (such as functional domains or external systems)
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
