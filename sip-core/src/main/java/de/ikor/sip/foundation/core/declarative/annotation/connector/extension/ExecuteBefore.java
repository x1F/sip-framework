package de.ikor.sip.foundation.core.declarative.annotation.connector.extension;

import de.ikor.sip.foundation.core.declarative.connector.ConnectorExtension;
import de.ikor.sip.foundation.core.declarative.connector.ConnectorProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used for relative ordering of {@link ConnectorExtension}s.
 *
 * <p>Specifies that the annotated processor should be run before the extension specified by either
 * {@link #value()} or {@link #extensionName()}.
 *
 * @see ExecuteAfter
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecuteBefore {
  /**
   * @return Reference to connector after which the annotated connector should run
   */
  Class<? extends ConnectorExtension> value() default ConnectorProcessor.None.class;

  /**
   * @return Name of the connector name before which the annotated connector should run
   */
  String extensionName() default "";
}
