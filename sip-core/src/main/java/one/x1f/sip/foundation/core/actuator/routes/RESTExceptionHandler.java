package one.x1f.sip.foundation.core.actuator.routes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** Controller advice for handling IncompatibleOperationException */
@ControllerAdvice
public class RESTExceptionHandler {

  /**
   * Handles {@link IncompatibleOperationException}
   *
   * @param ex {@link Exception}
   * @param request {@link HttpServletRequest}
   * @param response {@link HttpServletResponse}
   * @return status 400 with message containing available operations
   */
  @ExceptionHandler(IncompatibleOperationException.class)
  public ResponseEntity<Object> handle(
      Exception ex, HttpServletRequest request, HttpServletResponse response) {
    return new ResponseEntity<>(
        "Invalid operation! Available operations: start, stop, suspend, resume.",
        HttpStatus.BAD_REQUEST);
  }
}
