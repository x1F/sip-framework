package de.ikor.sip.foundation.core.declarative.model;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.ikor.sip.foundation.core.util.exception.SIPFrameworkException;
import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.Message;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ModelMapperTest {

  static final ModelMapper<Integer, String> MAPPER =
      new ModelMapper<Integer, String>() {
        @Override
        public String mapToTargetModel(Integer sourceModel) {
          return sourceModel.toString();
        }
      };

  @Test
  void WHEN_mapping_fails_VERIFY_exception_is_thrown_and_wrapped() throws InvalidPayloadException {
    var exchange = mock(Exchange.class);
    var message = mock(Message.class);
    when(exchange.getMessage()).thenReturn(message);
    when(message.getMandatoryBody(any())).thenThrow(InvalidPayloadException.class);
    Assertions.assertThatExceptionOfType(SIPFrameworkException.class)
        .isThrownBy(() -> MAPPER.process(exchange));
  }
}
