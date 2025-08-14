package one.x1f.sip.foundation.core.util;

import java.io.IOException;
import java.util.Objects;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.lang.NonNull;
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
      @Nullable String name, @NonNull EncodedResource encodedResource) throws IOException {
    YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
    factory.setResources(encodedResource.getResource());
    return new PropertiesPropertySource(
        name == null ? Objects.requireNonNull(encodedResource.getResource().getFilename()) : name,
        Objects.requireNonNull(factory.getObject()));
  }
}
