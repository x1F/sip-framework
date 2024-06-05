package de.ikor.sip.foundation.core.declarative.annotation.connector;

import de.ikor.sip.foundation.core.declarative.connector.ConnectorProcessor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RunAfterConnectorProcessor {
  Class<? extends ConnectorProcessor> value() default ConnectorProcessor.None.class;

  String processorName() default "";
}
