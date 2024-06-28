package de.ikor.sip.foundation.core.declarative.annonation;

import de.ikor.sip.foundation.core.declarative.model.ModelMapper;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for connectors extending {@link
 * de.ikor.sip.foundation.core.declarative.connector.ConnectorBase} to attach an automatic model
 * mapper transformation for the request
 *
 * @see UseResponseModelMapper
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UseRequestModelMapper {

  /**
   * @return (Optional) {@link ModelMapper} to use
   */
  Class<? extends ModelMapper> value();
}
