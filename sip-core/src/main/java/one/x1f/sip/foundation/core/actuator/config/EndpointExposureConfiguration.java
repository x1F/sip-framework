package one.x1f.sip.foundation.core.actuator.config;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import lombok.Setter;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Collects all SIP endpoint that should be exposed in actuator and adds them to
 * "management.endpoints.web.exposure.include"
 */
public class EndpointExposureConfiguration
    implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

  private static final String EXPOSURE_INCLUDE = "management.endpoints.web.exposure.include";
  private static final String SIP_DEFAULTS = "sip.core.actuator.endpoints";
  @Setter private String path;

  /**
   * Creates new instance of EndpointExposureConfiguration and sets default path to configuration
   * file
   */
  public EndpointExposureConfiguration() {
    setPath("sip-core-default-config.yaml");
  }

  @Override
  public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
    ConfigurableEnvironment environment = event.getEnvironment();
    Properties exposedEndpoints = new Properties();
    // get properties from SIP configuration files that are not yet loaded when the event is
    // triggered
    Properties loadedProperties = loadDefaultProperties();
    String endpoints = resolveEndpoints(environment, loadedProperties);
    exposedEndpoints.put(EXPOSURE_INCLUDE, endpoints);
    PropertiesPropertySource endpointsPropertySource =
        new PropertiesPropertySource("exposedEndpoints", exposedEndpoints);
    environment.getPropertySources().addFirst(endpointsPropertySource);
  }

  private String resolveEndpoints(
      ConfigurableEnvironment environment, Properties loadedProperties) {
    String endpoints =
        Objects.requireNonNullElse(
            environment.getProperty(EXPOSURE_INCLUDE),
            loadedProperties.getProperty(EXPOSURE_INCLUDE));
    if (!"*".equals(endpoints)) {
      String sipEndpoints = loadedProperties.getProperty(SIP_DEFAULTS);
      endpoints = endpoints.concat("," + sipEndpoints);
    }
    return endpoints;
  }

  private Properties loadDefaultProperties() {
    YamlPropertiesFactoryBean yamlPropertiesFactory = new YamlPropertiesFactoryBean();
    yamlPropertiesFactory.setResources(getResources());
    return Objects.requireNonNull(yamlPropertiesFactory.getObject());
  }

  private Resource[] getResources() {
    ClassLoader classLoader = EndpointExposureConfiguration.class.getClassLoader();
    ResourcePatternResolver resourcePatternResolver =
        new PathMatchingResourcePatternResolver(classLoader);
    try {
      return resourcePatternResolver.getResources(path);
    } catch (IOException e) {
      throw SIPFrameworkException.init("File not found for path %s", path);
    }
  }
}
