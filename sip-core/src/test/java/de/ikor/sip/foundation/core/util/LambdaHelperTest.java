package de.ikor.sip.foundation.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import lombok.experimental.StandardException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class LambdaHelperTest {

  @StandardException
  static class CheckedException extends Exception {}
  ;

  @Test
  void GIVEN_string_comparator_WHEN_wrapping_as_lambda_VERIFY_order_is_equal() {

    final var elements = List.of("one", "two", "three");
    final LambdaHelper.ThrowingComparator<String> lambdaCompare =
        (first, second) -> comparatorThatThrowsChecked(first, second, false);

    final var orderedByLambda = elements.stream().sorted(lambdaCompare).toList();
    final var orderedByInner =
        elements.stream()
            .sorted(
                new Comparator<String>() {
                  @Override
                  public int compare(String o1, String o2) {
                    try {
                      return comparatorThatThrowsChecked(o1, o2, false);
                    } catch (CheckedException e) {
                      throw new RuntimeException(e);
                    }
                  }
                })
            .toList();

    assertThat(orderedByLambda)
        .containsExactlyInAnyOrderElementsOf(elements)
        .isEqualTo(orderedByInner);
  }

  @Test
  void GIVEN_string_comparator_WHEN_wrapping_as_lambda_VERIFY_exception_is_wrapped_as_cause() {
    final var elements = List.of("one", "two", "three");
    final LambdaHelper.ThrowingComparator<String> lambdaThrowing =
        (first, second) -> comparatorThatThrowsChecked(first, second, true);

    assertThatRuntimeException()
        .isThrownBy(() -> elements.stream().sorted(lambdaThrowing).toList())
        .withCauseInstanceOf(CheckedException.class);
  }

  @Test
  void GIVEN_string_comparator_WHEN_wrapping_as_lambda_VERIFY_runtimeexception_is_passed_through() {
    final var elements = new String[] {"one", "two", null};
    final LambdaHelper.ThrowingComparator<String> lambdaNpe =
        (first, second) -> comparatorThatThrowsChecked(first, second, false);

    Assertions.assertThatNullPointerException()
        .isThrownBy(() -> Arrays.stream(elements).sorted(lambdaNpe).toList());
  }

  static int comparatorThatThrowsChecked(
      final String first, final String second, final boolean doThrow) throws CheckedException {
    if (doThrow) {
      throw new CheckedException();
    }
    return first.compareTo(second);
  }
}
