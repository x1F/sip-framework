package one.x1f.sip.foundation.core.declarative.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import one.x1f.sip.foundation.core.declarative.connector.ConnectorBase;
import one.x1f.sip.foundation.core.declarative.model.ModelMapper;

/**
 * Annotation for connectors extending {@link ConnectorBase} to attach an automatic model mapper
 * transformation for the response
 *
 * @see UseRequestModelMapper
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UseResponseModelMapper {

  /**
   * @return {@link ModelMapper} to use
   */
  Class<? extends ModelMapper> value();
}
