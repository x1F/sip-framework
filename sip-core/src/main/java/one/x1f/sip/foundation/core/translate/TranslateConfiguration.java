package one.x1f.sip.foundation.core.translate;

import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Config properties for defining all parameters important for translate service and message source
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "sip.core.translation")
public class TranslateConfiguration {
  private List<String> fileLocations = new LinkedList<>();
  private List<String> sipFileLocations;
  private String lang = "en";
  private String defaultEncoding = "UTF-8";
  private Boolean fallbackToSystemLocale = false;
  private Boolean useCodeAsDefaultMessage = true;

  /**
   * Defines and configures a {@link MessageSource}
   *
   * @return {@link MessageSource}
   */
  @Bean
  @Primary
  public MessageSource messageSource() {
    ReloadableResourceBundleMessageSource messageSource =
        new ReloadableResourceBundleMessageSource();
    fileLocations.addAll(sipFileLocations);
    for (String baseName : this.getFileLocations()) {
      messageSource.addBasenames(baseName);
    }
    messageSource.setDefaultEncoding(this.getDefaultEncoding());
    messageSource.setFallbackToSystemLocale(this.getFallbackToSystemLocale());
    messageSource.setUseCodeAsDefaultMessage(this.getUseCodeAsDefaultMessage());
    return messageSource;
  }
}
