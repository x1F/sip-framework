package one.x1f.sip.foundation.core.translate;

import java.util.Locale;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

/** Service used to get translated messages */
@Service
public class SIPTranslateMessageService implements InitializingBean {
  private final Locale language;
  private final MessageSource messageSource;
  private static SIPTranslateMessageService instance;

  /**
   * Creates new instance of {@link SIPTranslateMessageService}
   *
   * @param messageSource {@link MessageSource}
   * @param translateConfiguration {@link TranslateConfiguration}
   */
  @Autowired
  public SIPTranslateMessageService(
      MessageSource messageSource, TranslateConfiguration translateConfiguration) {
    this.messageSource = messageSource;
    this.language = new Locale(translateConfiguration.getLang());
  }

  /**
   * Get a translated message by providing its key
   *
   * @param key a key to which a message is bound
   * @return a translated message
   */
  public String getTranslatedMessage(String key) {
    return messageSource.getMessage(key, null, language);
  }

  /**
   * Get a translated message by providing its key and message arguments
   *
   * @param key a key to which a message is bound
   * @param args an array of arguments that will be filled in for params within the message
   * @return translated message
   */
  public String getTranslatedMessage(String key, Object... args) {
    return messageSource.getMessage(key, args, language);
  }

  @Override
  public void afterPropertiesSet() {
    synchronized (this) {
      SIPTranslateMessageService.instance = this;
    }
  }

  public static SIPTranslateMessageService get() {
    return instance;
  }
}
