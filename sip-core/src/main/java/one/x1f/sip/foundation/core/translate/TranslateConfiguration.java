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
  /** Enable SIP translation */
  private boolean enabled = true;

  /** Sets locations of translation bundles */
  private List<String> fileLocations = new LinkedList<>();

  private List<String> sipFileLocations;

  /** Set language of log messages */
  private String lang = "en";

  /** Sets default encoding */
  private String defaultEncoding = "UTF-8";

  /** Use system language if none defined */
  private Boolean fallbackToSystemLocale = false;

  /** If key is not assigned use it in message */
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
