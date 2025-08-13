package one.x1f.sip.foundation.core.translate.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/** Message shouldn't be set because it cannot be overridden due to LoggingEvent#setMessage */
@Mapper
public interface EventLogCloner {
  /**
   * Map message
   *
   * @param e {@link ILoggingEvent}
   * @return {@link LoggingEvent}
   */
  @Mapping(target = "message", ignore = true)
  @Mapping(target = "mdc", ignore = true)
  @Mapping(
      source = "throwableProxy",
      target = "throwableProxy",
      qualifiedByName = "castIThrowableProxy")
  LoggingEvent mapWithNoMessage(ILoggingEvent e);

  /**
   * Casts {@link IThrowableProxy} into {@link ThrowableProxy}
   *
   * @param castIThrowableProxy {@link IThrowableProxy}
   * @return {@link ThrowableProxy}
   */
  @Named("castIThrowableProxy")
  static ThrowableProxy castIThrowableProxy(IThrowableProxy castIThrowableProxy) {
    return (ThrowableProxy) castIThrowableProxy;
  }
}
