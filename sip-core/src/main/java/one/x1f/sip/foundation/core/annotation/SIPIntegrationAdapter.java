package one.x1f.sip.foundation.core.annotation;

import java.lang.annotation.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.annotation.AliasFor;

/**
 * Puts together {@link SpringBootApplication} and all other annotations typically needed when
 * developing SIP Integration Adapter.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootApplication
public @interface SIPIntegrationAdapter {

  /**
   * Alias for exclude in {@link SpringBootApplication}
   *
   * @return class
   */
  @AliasFor(annotation = SpringBootApplication.class)
  Class<?>[] exclude() default {};

  /**
   * Alias for excludeName in {@link SpringBootApplication}
   *
   * @return class
   */
  @AliasFor(annotation = SpringBootApplication.class)
  String[] excludeName() default {};

  /**
   * Alias for basePackages in {@link SpringBootApplication}
   *
   * @return class
   */
  @AliasFor(annotation = SpringBootApplication.class, attribute = "scanBasePackages")
  String[] basePackages() default {};

  /**
   * Alias for proxyBeanMethods in {@link SpringBootApplication}
   *
   * @return class
   */
  @AliasFor(annotation = SpringBootApplication.class)
  boolean proxyBeanMethods() default true;
}
