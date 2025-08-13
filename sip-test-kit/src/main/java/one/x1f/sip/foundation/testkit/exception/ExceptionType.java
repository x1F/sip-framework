package one.x1f.sip.foundation.testkit.exception;

/** Types of possible exceptions for {@link TestCaseInitializationException} */
public enum ExceptionType {
  MOCK("mock"),
  RESULT_VALIDATOR("result validator");

  private final String value;

  ExceptionType(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return this.value;
  }
}
