package de.ikor.sip.foundation.core.declarative.annotation.connector;

import de.ikor.sip.foundation.core.declarative.connector.ConnectorDefinition;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConnectorResponseProcessor {

  Class<? extends ConnectorDefinition> value() default ConnectorDefinition.None.class;

  String connectorId() default "";
}
