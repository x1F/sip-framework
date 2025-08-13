package one.x1f.sip.foundation.core.translate.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.pattern.PatternLayoutEncoderBase;

/**
 * Extends PatternLayoutEncoderBase in order to enhance it with message translation feature by
 * featuring {@link TranslateMessageLayout} .
 */
public class SIPPatternLayoutEncoder extends PatternLayoutEncoderBase<ILoggingEvent> {

  @Override
  public void start() {
    TranslateMessageLayout<LoggingEvent> patternLayout = new TranslateMessageLayout<>();
    patternLayout.setOutputPatternAsHeader(outputPatternAsHeader);
    patternLayout.setPattern(getPattern());
    patternLayout.setContext(context);
    patternLayout.start();
    this.layout = patternLayout;
    super.start();
  }
}
