package one.x1f.sip.foundation.testkit.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileNotFoundException;
import one.x1f.sip.foundation.testkit.configurationproperties.models.MessageProperties;
import org.junit.jupiter.api.Test;

class MessagePropertiesTest {
  MessageProperties subject = new MessageProperties();

  @Test
  void when_BodyReferencesFile_thenBodyContentIsReadFromFile() {
    subject.getBody().setValue("classpath:body.json");
    FileNotFoundException fileNotFoundException =
        assertThrows(FileNotFoundException.class, () -> subject.getBodyAsString());
    assertThat(fileNotFoundException.getMessage()).contains("body.json");
  }
}
