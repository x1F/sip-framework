package one.x1f.sip.foundation.core.translate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

class SIPTranslateMessageServiceTest {

  private static final String MESSAGE_KEY = "key";
  private static final String MESSAGE_CONTENT = "test";
  private static final String LANG = "en";

  private SIPTranslateMessageService subject;
  private MessageSource messageSource;
  private TranslateConfiguration translateConfiguration;

  @BeforeEach
  void setUp() {
    messageSource = mock(MessageSource.class);
    translateConfiguration = mock(TranslateConfiguration.class);
    when(translateConfiguration.getLang()).thenReturn(LANG);
    subject = new SIPTranslateMessageService(messageSource, translateConfiguration);
  }

  @Test
  void When_getTranslatedMessageWithoutArguments_Expect_TranslatedMessage() {
    // arrange
    when(messageSource.getMessage(MESSAGE_KEY, null, new Locale(LANG))).thenReturn(MESSAGE_CONTENT);

    // assert
    assertThat(subject.getTranslatedMessage(MESSAGE_KEY)).isEqualTo(MESSAGE_CONTENT);
  }

  @Test
  void When_getTranslatedMessageWithArguments_Expect_translatedMessage() {
    // arrange
    Object[] array = new Object[1];
    when(messageSource.getMessage(MESSAGE_KEY, array, new Locale(LANG)))
        .thenReturn(MESSAGE_CONTENT);

    // assert
    assertThat(subject.getTranslatedMessage(MESSAGE_KEY, array)).isEqualTo(MESSAGE_CONTENT);
  }

  @Test
  void When_InstanceIsSet_Expect_Instance() {
    // act
    subject.afterPropertiesSet();

    // assert
    assertThat(SIPTranslateMessageService.get()).isEqualTo(subject);

    // clear static instance to prevent issues with other tests
    ReflectionTestUtils.setField(subject, "instance", null);
  }
}
