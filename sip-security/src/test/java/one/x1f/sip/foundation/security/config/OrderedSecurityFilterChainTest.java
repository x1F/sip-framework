package one.x1f.sip.foundation.security.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.SecurityFilterChain;

class OrderedSecurityFilterChainTest {

  @Test
  void When_CallDelegatedMethod_Expect_SourceMethodsCalled() {
    // arrange
    SecurityFilterChain delegate =
        new SecurityFilterChain() {

          @Override
          public boolean matches(HttpServletRequest request) {
            request.setAttribute("matchesCalled", true);
            return true;
          }

          @Override
          public List<Filter> getFilters() {
            return List.of();
          }
        };
    int order = 42;
    OrderedSecurityFilterChain subject = new OrderedSecurityFilterChain(order, delegate);

    // act + assert
    assertThat(subject.getOrder()).isEqualTo(order);

    MockHttpServletRequest request = new MockHttpServletRequest();
    assertThat(subject.matches(request)).isTrue();
    assertThat(request.getAttribute("matchesCalled")).isEqualTo(true);

    assertThat(subject.getFilters()).isEmpty();
  }
}
