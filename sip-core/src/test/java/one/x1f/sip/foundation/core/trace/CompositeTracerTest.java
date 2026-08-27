package one.x1f.sip.foundation.core.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class CompositeTracerTest {

  CompositeTracer compositeTracer = new CompositeTracer(new ArrayList<>());

  @Test
  void shouldTrace() {
    assertThat(compositeTracer.shouldTrace(null)).isTrue();
  }

  @Test
  void getTraceCounter() {
    assertThat(compositeTracer.getTraceCounter()).isZero();
  }

  @Test
  void resetTraceCounter() {
    assertDoesNotThrow(() -> compositeTracer.resetTraceCounter());
  }

  @Test
  void setEnabled() {
    assertDoesNotThrow(() -> compositeTracer.setEnabled(true));
  }

  @Test
  void isStandby() {
    assertThat(compositeTracer.isStandby()).isFalse();
  }

  @Test
  void setStandby() {
    assertDoesNotThrow(() -> compositeTracer.setStandby(true));
  }

  @Test
  void setTraceRests() {
    assertDoesNotThrow(() -> compositeTracer.setTraceRests(true));
  }

  @Test
  void isTraceTemplates() {
    assertThat(compositeTracer.isTraceTemplates()).isFalse();
  }

  @Test
  void setTraceTemplates() {
    assertDoesNotThrow(() -> compositeTracer.setTraceTemplates(true));
  }

  @Test
  void getTracePattern() {
    assertThat(compositeTracer.getTracePattern()).isEmpty();
  }

  @Test
  void setTracePattern() {
    assertDoesNotThrow(() -> compositeTracer.setTracePattern(null));
  }

  @Test
  void isTraceBeforeAndAfterRoute() {
    assertThat(compositeTracer.isTraceBeforeAndAfterRoute()).isFalse();
  }

  @Test
  void setTraceBeforeAndAfterRoute() {
    assertDoesNotThrow(() -> compositeTracer.setTraceBeforeAndAfterRoute(true));
  }

  @Test
  void getExchangeFormatter() {
    assertThat(compositeTracer.getExchangeFormatter()).isNull();
  }

  @Test
  void setExchangeFormatter() {
    assertDoesNotThrow(() -> compositeTracer.setExchangeFormatter(null));
  }

  @Test
  void stop() {
    assertDoesNotThrow(() -> compositeTracer.stop());
  }
}
