package one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker;

import static one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.SIPJmsTextMessage.*;
import static org.apache.camel.component.jms.JmsConstants.JMS_X_GROUP_ID;
import static org.assertj.core.api.Assertions.*;

import jakarta.jms.Message;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SIPJmsTextMessageTest {

  private static final String TEST = "test";

  private SIPJmsTextMessage subject;

  @BeforeEach
  void setup() {
    subject = new SIPJmsTextMessage(null);
  }

  @Test
  void GIVEN_noInputs_WHEN_callingGetters_THEN_expectDefaultValues() {
    // act & assert
    assertThat(subject.getText()).isNull();
    assertThat(subject.getJMSCorrelationID()).isNull();
    assertThat(subject.getJMSCorrelationIDAsBytes()).isNull();
    assertThat(subject.getJMSDestination()).isNull();
    assertThat(subject.getJMSMessageID()).isNull();
    assertThat(subject.getJMSRedelivered()).isFalse();
    assertThat(subject.getJMSReplyTo()).isNull();
    assertThat(subject.getJMSType()).isNull();
    assertThat(subject.getObjectProperty(JMS_X_GROUP_ID)).isNull();
    assertThat(subject.getObjectProperty(JMS_X_USER_ID)).isNull();
    assertThat(subject.getJMSDeliveryMode()).isEqualTo(Message.DEFAULT_DELIVERY_MODE);
    assertThat(subject.getJMSExpiration()).isEqualTo(Message.DEFAULT_TIME_TO_LIVE);
    assertThat(subject.getJMSPriority()).isEqualTo(Message.DEFAULT_PRIORITY);
    assertThat(subject.getJMSTimestamp()).isLessThanOrEqualTo(System.currentTimeMillis());
  }

  @Test
  void GIVEN_overridingInputs_WHEN_callingGetters_THEN_expectInputValues() {
    // arrange
    subject.setText("text value");
    String jmsCorrelationID = "123";
    subject.setJMSCorrelationIDAsBytes(null);
    subject.setJMSCorrelationID(jmsCorrelationID);
    subject.setJMSMessageID("12345");
    subject.setJMSRedelivered(true);
    subject.setJMSType("message");
    subject.setObjectProperty(JMS_X_GROUP_ID, "10");
    subject.setObjectProperty(JMS_X_USER_ID, "10");
    subject.setJMSDestination(null);
    subject.setJMSReplyTo(null);
    subject.setJMSDeliveryMode(1);
    subject.setJMSExpiration(200);
    subject.setJMSPriority(6);
    subject.setJMSTimestamp(200);

    // act & assert
    assertThat(subject.getText()).isEqualTo("text value");
    assertThat(subject.getJMSCorrelationID()).isEqualTo(jmsCorrelationID);
    assertThat(subject.getJMSCorrelationIDAsBytes())
        .isEqualTo(jmsCorrelationID.getBytes(StandardCharsets.UTF_8));
    assertThat(subject.getJMSMessageID()).isEqualTo("12345");
    assertThat(subject.getJMSRedelivered()).isTrue();
    assertThat(subject.getJMSType()).isEqualTo("message");
    assertThat(subject.getObjectProperty(JMS_X_GROUP_ID)).isEqualTo("10");
    assertThat(subject.getObjectProperty(JMS_X_USER_ID)).isEqualTo("10");
    assertThat(subject.getJMSDestination()).isNull();
    assertThat(subject.getJMSReplyTo()).isNull();
    assertThat(subject.getJMSDeliveryMode()).isEqualTo(1);
    assertThat(subject.getJMSExpiration()).isEqualTo(200);
    assertThat(subject.getJMSPriority()).isEqualTo(6);
    assertThat(subject.getJMSTimestamp()).isEqualTo(200);
  }

  @Test
  void GIVEN_noInputs_WHEN_callingNonUsableGettersAndSetters_THEN_expectDefaultValues() {
    // act & assert
    assertThat(subject.propertyExists(TEST)).isFalse();
    assertThat(subject.getBooleanProperty(TEST)).isFalse();
    assertThat(subject.getByteProperty(TEST)).isZero();
    assertThat(subject.getShortProperty(TEST)).isZero();
    assertThat(subject.getIntProperty(TEST)).isZero();
    assertThat(subject.getLongProperty(TEST)).isZero();
    assertThat(subject.getFloatProperty(TEST)).isZero();
    assertThat(subject.getDoubleProperty(TEST)).isZero();
    assertThat(subject.getJMSDeliveryTime()).isZero();
    assertThat(subject.getBody(Object.class)).isNull();
    assertThat(subject.isBodyAssignableTo(null)).isFalse();
  }

  @Test
  void GIVEN_customHeaders_WHEN_getPropertyNames_THEN_expectCustomHeadersOnly() {
    // arrange
    subject.setObjectProperty("testKey1", TEST);
    subject.setObjectProperty("testKey2", TEST);
    subject.setObjectProperty("testKey3", TEST);

    // act
    Enumeration<String> names = subject.getPropertyNames();
    List<String> actualKeys = new ArrayList<>();
    while (names.hasMoreElements()) {
      actualKeys.add(names.nextElement());
    }

    // assert
    assertThat(actualKeys)
        .hasSize(3)
        .contains("testKey1")
        .contains("testKey2")
        .contains("testKey3");
  }

  @Test
  void GIVEN_keyWithNullValue_WHEN_getStringProperty_THEN_expectNullValue() {
    // act & assert
    assertThat(subject.getStringProperty(JMS_MESSAGE_ID)).isNull();
  }

  @Test
  void GIVEN_keyWithStringValue_WHEN_getStringProperty_THEN_expectStringValue() {
    // arrange
    subject.setObjectProperty("testKey", TEST);

    // act & assert
    assertThat(subject.getStringProperty("testKey")).isEqualTo(TEST);
  }
}
