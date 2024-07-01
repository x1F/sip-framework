package de.ikor.sip.foundation.core.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StreamHelper {

  /**
   * Filters a stream of objects by type.
   *
   * <p>Usage example: <code>List&lt;Object&gt;.stream().flatMap(typeFilter(String.class)).toList()
   * </code>
   *
   * @param type The filtering type
   * @return Filtering function
   * @param <S> The type of the stream
   * @param <T> Filtering type
   */
  public static <S, T extends S> Function<S, Stream<T>> typeFilter(final Class<T> type) {
    return entry -> type.isInstance(entry) ? Stream.of(type.cast(entry)) : Stream.empty();
  }

  /**
   * Returns at most one element from the given <code>stream</code> matching the given <code>
   * predicate</code>, but throws the exception retrieved from <code>multipleException</code> if
   * more match.
   *
   * @param stream Stream to retrieve unique element from
   * @param predicate Predicate for filtering
   * @param multipleException Error thrown if more than one element matches the predicate
   * @return Optional element found
   * @param <T> Element type
   */
  public static <T> Optional<T> findAtMostOne(
      final Stream<T> stream,
      final Predicate<T> predicate,
      final Supplier<RuntimeException> multipleException) {
    return stream
        .filter(predicate)
        .reduce(
            (dont, care) -> {
              throw multipleException.get();
            });
  }
}
