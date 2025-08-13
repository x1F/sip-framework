package one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker;

import static org.apache.camel.component.jms.JmsConstants.JMS_X_GROUP_ID;

import jakarta.jms.Destination;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * SIP implementation of TextMessage from JMS API. Used in Test Kit as a jms Message general
 * implementation in order to eliminate dependency on concrete jms broker.
 */
public class SIPJmsTextMessage implements TextMessage {

  public static final String JMS_MESSAGE_ID = "JMSMessageID";
  public static final String JMS_CORRELATION_ID = "JMSCorrelationID";
  public static final String JMS_CORRELATION_ID_AS_BYTES = "JMSCorrelationIDAsBytes";
  public static final String JMS_REPLY_TO = "JMSReplyTo";
  public static final String JMS_DESTINATION = "JMSDestination";
  public static final String JMS_REDELIVERED = "JMSRedelivered";
  public static final String JMS_TYPE = "JMSType";
  public static final String JMS_TIMESTAMP = "JMSTimestamp";
  public static final String JMS_DELIVERY_MODE = "JMSDeliveryMode";
  public static final String JMS_EXPIRATION = "JMSExpiration";
  public static final String JMS_PRIORITY = "JMSPriority";
  public static final String JMS_X_USER_ID = "JMSXUserID";

  private String text;
  private final Map<String, Serializable> jmsProperties;
  private final Map<String, Serializable> customProperties;

  public SIPJmsTextMessage(String text) {
    this.text = text;
    this.jmsProperties = new HashMap<>();
    this.customProperties = new HashMap<>();
    setupJmsProperties();
  }

  private void setupJmsProperties() {
    jmsProperties.put(JMS_CORRELATION_ID, null);
    jmsProperties.put(JMS_DELIVERY_MODE, Message.DEFAULT_DELIVERY_MODE);
    jmsProperties.put(JMS_DESTINATION, null);
    jmsProperties.put(JMS_EXPIRATION, Message.DEFAULT_TIME_TO_LIVE);
    jmsProperties.put(JMS_MESSAGE_ID, null);
    jmsProperties.put(JMS_PRIORITY, Message.DEFAULT_PRIORITY);
    jmsProperties.put(JMS_REDELIVERED, false);
    jmsProperties.put(JMS_TIMESTAMP, System.currentTimeMillis());
    jmsProperties.put(JMS_REPLY_TO, null);
    jmsProperties.put(JMS_TYPE, null);
    jmsProperties.put(JMS_X_GROUP_ID, null);
    jmsProperties.put(JMS_X_USER_ID, null);
  }

  @Override
  public void setText(String text) {
    this.text = text;
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public String getJMSMessageID() {
    return (String) jmsProperties.get(JMS_MESSAGE_ID);
  }

  @Override
  public void setJMSMessageID(String jmsMessageID) {
    jmsProperties.put(JMS_MESSAGE_ID, jmsMessageID);
  }

  @Override
  public long getJMSTimestamp() {
    return (long) jmsProperties.get(JMS_TIMESTAMP);
  }

  @Override
  public void setJMSTimestamp(long jmsTimestamp) {
    jmsProperties.put(JMS_TIMESTAMP, jmsTimestamp);
  }

  @Override
  public String getJMSCorrelationID() {
    return (String) jmsProperties.get(JMS_CORRELATION_ID);
  }

  @Override
  public void setJMSCorrelationID(String jmsCorrelationID) {
    jmsProperties.put(JMS_CORRELATION_ID, jmsCorrelationID);
  }

  @Override
  public byte[] getJMSCorrelationIDAsBytes() {
    String correlationId = (String) jmsProperties.get(JMS_CORRELATION_ID);
    return correlationId != null ? correlationId.getBytes(StandardCharsets.UTF_8) : null;
  }

  @Override
  public void setJMSCorrelationIDAsBytes(byte[] jmsCorrelationIDAsBytes) {
    String id =
        jmsCorrelationIDAsBytes != null
            ? new String(jmsCorrelationIDAsBytes, StandardCharsets.UTF_8)
            : null;
    jmsProperties.put(JMS_CORRELATION_ID, id);
  }

  @Override
  public Destination getJMSReplyTo() {
    return (Destination) jmsProperties.get(JMS_REPLY_TO);
  }

  @Override
  public void setJMSReplyTo(Destination jmsReplyTo) {
    jmsProperties.put(JMS_REPLY_TO, (Serializable) jmsReplyTo);
  }

  @Override
  public Destination getJMSDestination() {
    return (Destination) jmsProperties.get(JMS_DESTINATION);
  }

  @Override
  public void setJMSDestination(Destination jmsDestination) {
    jmsProperties.put(JMS_DESTINATION, (Serializable) jmsDestination);
  }

  @Override
  public int getJMSDeliveryMode() {
    return (int) jmsProperties.get(JMS_DELIVERY_MODE);
  }

  @Override
  public void setJMSDeliveryMode(int jmsDeliveryMode) {
    jmsProperties.put(JMS_DELIVERY_MODE, jmsDeliveryMode);
  }

  @Override
  public boolean getJMSRedelivered() {
    return (boolean) jmsProperties.get(JMS_REDELIVERED);
  }

  @Override
  public void setJMSRedelivered(boolean jmsRedelivered) {
    jmsProperties.put(JMS_REDELIVERED, jmsRedelivered);
  }

  @Override
  public String getJMSType() {
    return (String) jmsProperties.get(JMS_TYPE);
  }

  @Override
  public void setJMSType(String jmsType) {
    jmsProperties.put(JMS_TYPE, jmsType);
  }

  @Override
  public long getJMSExpiration() {
    return (long) jmsProperties.get(JMS_EXPIRATION);
  }

  @Override
  public void setJMSExpiration(long jmsExpiration) {
    jmsProperties.put(JMS_EXPIRATION, jmsExpiration);
  }

  @Override
  public int getJMSPriority() {
    return (int) jmsProperties.get(JMS_PRIORITY);
  }

  @Override
  public void setJMSPriority(int jmsPriority) {
    jmsProperties.put(JMS_PRIORITY, jmsPriority);
  }

  @Override
  public void setObjectProperty(String key, Object value) {
    if (jmsProperties.containsKey(key)) {
      jmsProperties.put(key, (Serializable) value);
    } else {
      customProperties.put(key, (Serializable) value);
    }
  }

  @Override
  public Object getObjectProperty(String key) {
    if (jmsProperties.containsKey(key)) {
      return jmsProperties.get(key);
    } else {
      return customProperties.get(key);
    }
  }

  @Override
  public Enumeration<String> getPropertyNames() {
    return new Vector<>(this.customProperties.keySet()).elements();
  }

  @Override
  public String getStringProperty(String key) {
    Object o = this.getObjectProperty(key);
    if (o == null) return null;
    else if (o instanceof String str) return str;
    else return o.toString();
  }

  @Override
  public boolean propertyExists(String s) {
    return false;
  }

  @Override
  public boolean getBooleanProperty(String s) {
    return false;
  }

  @Override
  public byte getByteProperty(String s) {
    return 0;
  }

  @Override
  public short getShortProperty(String s) {
    return 0;
  }

  @Override
  public int getIntProperty(String s) {
    return 0;
  }

  @Override
  public long getLongProperty(String s) {
    return 0;
  }

  @Override
  public float getFloatProperty(String s) {
    return 0;
  }

  @Override
  public double getDoubleProperty(String s) {
    return 0;
  }

  @Override
  public void clearProperties() {
    // Do nothing, implementation is not necessary
  }

  @Override
  public void setBooleanProperty(String s, boolean b) {
    // Do nothing, implementation is not necessary
  }

  @Override
  public void setByteProperty(String s, byte b) {
    // Do nothing, implementation is not necessary
  }

  @Override
  public void setShortProperty(String s, short i) {
    // Do nothing, implementation is not necessary
  }

  @Override
  public void setIntProperty(String s, int i) {
    // Do nothing, implementation is not necessary
  }

  @Override
  public void setLongProperty(String s, long l) {
    // Do nothing, implementation is not necessary
  }

  @Override
  public void setFloatProperty(String s, float v) {
    // Do nothing, implementation is not necessary
  }

  @Override
  public void setDoubleProperty(String s, double v) {
    // Do nothing, implementation is not necessary
  }

  @Override
  public void setStringProperty(String s, String s1) {
    // Do nothing, implementation is not necessary
  }

  @Override
  public void acknowledge() {
    // Do nothing, implementation is not necessary
  }

  @Override
  public void clearBody() {
    // Do nothing, implementation is not necessary
  }

  @Override
  public void setJMSDeliveryTime(long l) {
    // Do nothing, implementation is not necessary
  }

  @Override
  public long getJMSDeliveryTime() {
    return 0;
  }

  @Override
  public <T> T getBody(Class<T> aClass) {
    return null;
  }

  @Override
  public boolean isBodyAssignableTo(Class aClass) {
    return false;
  }
}
