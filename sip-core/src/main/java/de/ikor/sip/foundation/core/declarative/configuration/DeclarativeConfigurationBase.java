package de.ikor.sip.foundation.core.declarative.configuration;

import org.springframework.util.ClassUtils;

/** Interface which provides enables definition Apache Camels route configuration */
public interface DeclarativeConfigurationBase {

  /**
   * Define configuration
   *
   * @return {@link DeclarativeConfigurationDefinition}
   */
  DeclarativeConfigurationDefinition configure();

  /**
   * Provides simple class name and clears appended Spring wrapper name
   *
   * @return short class name
   */
  default String getName() {
    return ClassUtils.getShortName(this.getClass());
  }
}
