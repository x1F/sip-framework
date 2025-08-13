package one.x1f.sip.foundation.core.actuator.health;

import java.util.function.Predicate;
import org.apache.camel.Endpoint;
import org.springframework.util.AntPathMatcher;

/**
 * {@link PathMatcher} offers the ability to match Camel {@link Endpoint}s - actually their URIs,
 * with URI patterns implemented by the {@link AntPathMatcher}.
 */
class PathMatcher implements Predicate<Endpoint> {
  private final String pathMatcherExpression;
  private final org.springframework.util.PathMatcher matcher;

  /**
   * Creates a PathMatcher for the given expression.
   *
   * @param pathMatcherExpression path to match
   * @return Predicate
   */
  public static Predicate<Endpoint> of(String pathMatcherExpression) {
    return new PathMatcher(pathMatcherExpression);
  }

  PathMatcher(String pathMatcherExpression) {
    this.pathMatcherExpression = pathMatcherExpression;
    matcher = new AntPathMatcher();
  }

  @Override
  public boolean test(Endpoint endpoint) {
    String endpointUri = endpoint.getEndpointUri();
    return matcher.match(pathMatcherExpression, endpointUri);
  }

  public String getPathMatcherExpression() {
    return pathMatcherExpression;
  }
}
