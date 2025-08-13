package one.x1f.sip.foundation.core.translate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class TranslationServiceDetailsOnStartupTest {

  private TranslationServiceDetailsOnStartup subject;
  private TranslateConfiguration translateConfiguration;
  private List<String> fileLocations;
  private ListAppender<ILoggingEvent> listAppender;
  private List<ILoggingEvent> logsList;

  @BeforeEach
  void setup() {
    translateConfiguration = mock(TranslateConfiguration.class);
    when(translateConfiguration.getLang()).thenReturn("en");
    when(translateConfiguration.getDefaultEncoding()).thenReturn("UTF-8");
    subject = new TranslationServiceDetailsOnStartup(translateConfiguration);

    Logger logger =
        (Logger)
            LoggerFactory.getLogger(
                "one.x1f.sip.foundation.core.translate.TranslationServiceDetailsOnStartup");
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    logsList = listAppender.list;
    fileLocations = new ArrayList<>();
  }

  @Test
  void Given_ExistingBundle_When_logTranslationDetails_Then_bundleMessagesInLog() {
    // arrange
    fileLocations.add("classpath:sip-core-messages");
    when(translateConfiguration.getFileLocations()).thenReturn(fileLocations);

    // act
    subject.logTranslationDetails();

    // assert
    assertThat(logsList.get(3).getMessage()).isEqualTo("sip.core.translation.bundle_{}");
    assertThat(logsList.get(3).getLevel()).isEqualTo(Level.INFO);
  }

  @Test
  void Given_MissingBundle_When_logTranslationDetails_Then_missingBundleMessageInLog() {
    // arrange
    fileLocations.add("classpath:sa");
    when(translateConfiguration.getFileLocations()).thenReturn(fileLocations);

    // act
    subject.logTranslationDetails();

    // assert
    assertThat(logsList.get(3).getMessage()).isEqualTo("sip.core.translation.missingbundle_{}");
    assertThat(logsList.get(3).getLevel()).isEqualTo(Level.ERROR);
  }
}
