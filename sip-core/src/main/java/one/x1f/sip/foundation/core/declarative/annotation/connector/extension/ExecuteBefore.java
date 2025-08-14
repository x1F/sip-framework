package one.x1f.sip.foundation.core.declarative.annotation.connector.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import one.x1f.sip.foundation.core.declarative.connector.ConnectorExtension;

/**
 * Annotation used for relative ordering of {@link ConnectorExtension}s.
 *
 * <p>Specifies that the annotated extension should be run before the extension specified by either
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
  Class<? extends ConnectorExtension> value() default ConnectorExtension.None.class;

  /**
   * @return Name of the connector name before which the annotated connector should run
   */
  String extensionName() default "";
}
