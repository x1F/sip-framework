package one.x1f.sip.foundation.core.actuator.routes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class RESTExceptionHandlerTest {

  @Test
  void When_handleIncompatibleOperationException_Expect_BadRequest() {
    // arrange
    RESTExceptionHandler subject = new RESTExceptionHandler();

    // act
    ResponseEntity<Object> responseEntity =
        subject.handle(new IncompatibleOperationException("any"), null, null);

    // assert
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
}
