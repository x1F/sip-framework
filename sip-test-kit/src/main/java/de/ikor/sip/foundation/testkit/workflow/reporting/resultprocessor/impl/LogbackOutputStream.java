package de.ikor.sip.foundation.testkit.workflow.reporting.resultprocessor.impl;

import ch.qos.logback.classic.Logger;
import org.apache.commons.exec.LogOutputStream;
import org.slf4j.LoggerFactory;

/** OutputStream for logging test reports with logback */
public class LogbackOutputStream extends LogOutputStream {

  private static final String LOGGER_NAME = "TestReportLog";
  private final Logger logger;

  /** Adds logger that uses a pattern */
  public LogbackOutputStream() {
    logger = (Logger) LoggerFactory.getLogger(LOGGER_NAME);
  }

  @Override
  protected void processLine(String s, int i) {
    logger.error(s);
  }
}
