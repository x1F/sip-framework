package one.x1f.sip.foundation.testkit.workflow.reporting.resultprocessor.impl;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.spi.FilterReply;

/** Filter that can be used in a log configuration to filter loggers by name */
public class TestKitLogbackFilter extends AbstractMatcherFilter<ILoggingEvent> {
  private String loggerName;

  /**
   * Filters logs based on logger name
   *
   * @param event log event
   */
  @Override
  public FilterReply decide(ILoggingEvent event) {
    if (!isStarted()) {
      return FilterReply.NEUTRAL;
    }
    return event.getLoggerName().equals(loggerName) ? onMatch : onMismatch;
  }

  /**
   * Logger name setter, required by filter setup in logback.xml
   *
   * @param loggerName name of the logger
   */
  public void setLoggerName(String loggerName) {
    this.loggerName = loggerName;
  }

  /** Starts if loggerName is set */
  @Override
  public void start() {
    if (this.loggerName != null) {
      super.start();
    }
  }
}
