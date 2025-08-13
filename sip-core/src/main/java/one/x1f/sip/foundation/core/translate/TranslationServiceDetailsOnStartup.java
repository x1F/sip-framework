package one.x1f.sip.foundation.core.translate;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Logger for startup information for TranslateConfiguration */
@Slf4j
@Component
@AllArgsConstructor
public class TranslationServiceDetailsOnStartup {

  private final TranslateConfiguration translateConfiguration;

  /** Display TranslateConfiguration information after application startup */
  @EventListener(ApplicationStartedEvent.class)
  public void logTranslationDetails() {
    log.info("sip.core.translation.on");
    log.info("sip.core.translation.lang_{}", translateConfiguration.getLang());
    log.info("sip.core.translation.encoding_{}", translateConfiguration.getDefaultEncoding());
    translateConfiguration
        .getFileLocations()
        .forEach(
            location -> {
              try {
                ResourceBundle.getBundle(location.replace("classpath:", ""));
                log.info("sip.core.translation.bundle_{}", location);
              } catch (MissingResourceException ex) {
                log.error("sip.core.translation.missingbundle_{}", location);
              }
            });
  }
}
