package one.x1f.sip.foundation.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;

class YamlPropertySourceFactoryTest {

  private final YamlPropertSourceFactory subject = new YamlPropertSourceFactory();

  @Test
  void WHEN_createPropertySource_WITH_nullResource_THEN_IllegalArgument() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> subject.createPropertySource("name", null));
  }

  @Test
  void WHEN_createPropertySource_WITH_validResource_THEN_propertySourceReturned() throws Exception {
    // arrange
    Resource resource = new ClassPathResource("test-yaml-property-source-factory.yaml");
    EncodedResource encodedResource = new EncodedResource(resource);

    // act
    PropertySource<?> result = subject.createPropertySource("name", encodedResource);

    // assert
    assertThat(result.getProperty("sip1.hierarchy")).asString().isEqualTo("test1");
    assertThat(result.getProperty("sip2.hierarchy")).asString().isEqualTo("test2");
  }
}
