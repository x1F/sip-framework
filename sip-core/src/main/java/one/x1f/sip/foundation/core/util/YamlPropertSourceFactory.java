package one.x1f.sip.foundation.core.util;

import lombok.NonNull;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.lang.Nullable;

/**
 * Utility for allowing {@link org.springframework.context.annotation.PropertySource} annotations to
 * also read yaml files instead of only properties files
 *
 * @author thomas.stieglmaier
 */
public class YamlPropertSourceFactory implements PropertySourceFactory {

  @Override
  public PropertySource<?> createPropertySource(
      @Nullable String name, @NonNull EncodedResource resource) {
    YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
    factory.setResources(resource.getResource());
    return new PropertiesPropertySource(
        name == null ? resource.getResource().getFilename() : name, factory.getObject());
  }
}
